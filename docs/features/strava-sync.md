# Strava Sync

Two-way integration with [Strava](https://www.strava.com/): connect a Strava account via OAuth 2.0
and upload completed sessions as cycling activities. Lives under `feature/integrations/strava/`,
split into `api/` (cross-feature interfaces) and `impl/` (data + presentation).

## Capabilities

| Capability | Where |
|---|---|
| OAuth 2.0 authorization (Custom Tabs flow) | `oauth/StravaAuthIntentLauncher`, `oauth/StravaOAuthRedirectActivity` |
| Bearer token storage and auto-refresh | `data/api/StravaTokenProvider`, `data/source/local/StravaTokenLocalDataSource` |
| Granted-scope validation | `oauth/StravaOAuthCodeProcessor`, `oauth/StravaAuthEvents` |
| Session → GPX → Strava upload | `work/StravaUploadWorker` |
| Upload-status polling | `work/StravaPollWorker` |
| Per-session sync state | `data/source/local/StravaSyncLocalDataSource` (Room) |
| Connect/disconnect UI | `presentation/connect/` |
| Session-share Strava tab | `presentation/share/` |

## OAuth flow

```
StravaConnectScreen ──tap "Connect"──► StravaAuthIntentLauncher
                                        │  Custom Tabs (NO_HISTORY)
                                        ▼
                                  www.strava.com/oauth/authorize
                                        │  user authorizes, Strava
                                        │  redirects to deep link
                                        ▼
                          cyclingassistant://koflox.github.io/strava/callback
                                        │  Android intent-filter
                                        ▼
                          StravaOAuthRedirectActivity (Theme.NoDisplay, singleTask)
                                  ├─ extracts ?code & ?scope
                                  ├─ hands code to StravaOAuthCodeProcessor
                                  ├─ relaunches MainActivity (CLEAR_TOP)
                                  └─ finish() — synchronous
                                        │
                                        ▼
                          StravaOAuthCodeProcessor (singleton scope)
                                  ├─ validates granted scopes
                                  │   └─ missing → logout + emit hint
                                  └─ exchanges code → persists tokens
```

The redirect activity is intentionally a separate `Activity` (not a Compose destination) because
deep links from external processes can only target manifest-registered components. It must
`finish()` synchronously inside `onCreate` / `onNewIntent` — that's the contract for
`Theme.NoDisplay`. Any async work (token exchange) is handed off to a singleton-scoped
`SupervisorJob` so it survives the activity lifecycle.

After processing, `MainActivity` is brought back to the front via
`FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP` so Custom Tabs (which lives in our task)
is destroyed alongside the redirect activity in one step. Without this the user would land back
in a now-empty Custom Tabs window.

## Required scopes

Strava's consent screen presents scope checkboxes the user can untick. There is no OAuth
parameter that makes a scope mandatory, so we re-validate after the redirect:

| Scope | Purpose | Required |
|---|---|---|
| `read` | Athlete profile basics | Yes (always granted alongside others) |
| `activity:write` | Upload activities | **Yes** |
| `activity:read` | Read uploaded activities back (verify-on-open, view URL) | **Yes** |

If `activity:write` or `activity:read` is missing from the callback's `scope=` query parameter,
`StravaOAuthCodeProcessor` calls `authUseCase.logout()` (deletes the just-stored tokens) and
emits `StravaAuthHint.MissingRequiredScopes` via `StravaAuthEvents`. The Connect screen renders
this hint as a banner with a "Got it" dismiss button.

`approval_prompt=force` is set on the authorize URL so Strava's consent screen always shows up,
even when the user is already logged into Strava in the browser — otherwise users connecting a
second time wouldn't see what scopes the app is requesting.

## Sync state machine

```
                ┌───────────────┐
                │  NotSynced    │ (no row in DB OR explicit state)
                └──────┬────────┘
                       │ user taps "Sync to Strava"
                       ▼
                ┌───────────────┐
                │  Pending      │ (StravaUploadWorker enqueued)
                └──────┬────────┘
                       │ worker picks up
                       ▼
                ┌───────────────┐
                │  Uploading    │ (POST /uploads with multipart GPX)
                └──────┬────────┘
                       │
                  ┌────┴────────────┐
                  │                 │
       Strava=ready             Strava=processing
                  │                 │
                  ▼                 ▼
        ┌───────────────┐  ┌───────────────────┐
        │ Synced(id)    │  │ Processing(uploadId)
        └───────────────┘  └────────┬──────────┘
                                    │ StravaPollWorker
                                    │ (10s initial delay,
                                    │  exp backoff, max 5)
                                    ▼
                      Ready / Failed / Timeout → Synced(id)
                                                   or Error(reason)
```

`SessionSyncStatus` (in the api module) is a sealed interface used by both feature/session and
feature/integrations/strava UIs. The data layer keeps an additional `uploadId` field needed for
polling — exposed via `setProcessing(sessionId, uploadId)` rather than carried in the domain
model so the UI doesn't have to care about Strava's two-stage upload.

## Verify-on-open

Strava activities can be deleted on the web/app at any time. When the user opens the Strava
share tab on a `Synced` session, `StravaShareViewModel` triggers
`syncUseCase.verifySyncedActivity(sessionId)` which calls `GET /activities/{id}` — a 404 means
the activity is gone, and we clear the local sync row so the UI falls back to `NotSynced`. Any
other error (network, auth, etc.) is silently ignored — we only react to a definitive "deleted".

## Manual refresh cooldown

While in `Processing`, the user can tap "Refresh status" to skip the WorkManager backoff.
`StravaSyncUseCase.refreshStatus` performs an inline `GET /uploads/{uploadId}` (no worker
indirection) and applies the result. The button then enters a 10-second cooldown shown as
"Refresh in 9s", "Refresh in 8s", … to prevent spamming the API. Cooldown is local to the
ViewModel — purely a UX guard, not server-enforced.

## Activity type

Uploads pass `activity_type=ride` to Strava's `/uploads` endpoint. Without this hint Strava's
auto-detection sometimes classifies casual ride pace as a run.

## DI wiring

| Component | Scope | Why |
|---|---|---|
| `StravaTokenProvider` | `@Singleton` | Holds Bearer-token bridging logic between Room and Ktor's `Auth` plugin |
| `StravaOAuthCodeProcessor` | `@Singleton` | Owns its own application-scoped `CoroutineScope` so token exchange survives the redirect activity |
| `StravaAuthEvents` | `@Singleton` | Cross-component bus (processor → Connect VM) for auth hints |
| `StravaAuthUseCase` | `@Singleton` | Stateful indirection over auth repository + remote |
| `StravaSyncUseCase` | `@Singleton` | Holds work scheduling and inline-refresh logic |
| `StravaUploadApiImpl` | `@Singleton` | Wraps the authenticated Ktor client |

The authenticated `HttpClient` is provided via `@StravaAuthenticatedClient` qualifier — see
[Network](../infrastructure/network.md) for details on the two-client setup.

## Cross-feature bridges

| Bridge | Direction |
|---|---|
| `feature/bridge/session-strava` | session → strava (passing GPX input + activity name) |

The session module pushes per-session GPX data into `SessionGpxDataProvider`, which the upload
worker reads when scheduled.

## App registration

Strava limits new OAuth apps to **1 athlete** by default. To enable other users, request a
limit increase via the Strava developer console (up to 999 in one approval, beyond that requires
further review). See [Setup](../product/setup.md#strava) for credential configuration.
