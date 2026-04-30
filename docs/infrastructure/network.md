# Network

The app's only outgoing network traffic goes to Strava's REST API. The HTTP layer lives entirely
inside `feature/integrations/strava/impl/data/api/` — no shared network module exists, since
there is exactly one external service to talk to.

## Stack

| Layer | Choice |
|---|---|
| HTTP client | [Ktor Client](https://ktor.io/docs/client.html) |
| Engine | OkHttp |
| Serialization | `kotlinx.serialization` (JSON) |
| Auth | Ktor `Auth` plugin (Bearer scheme) |
| Logging | Ktor `Logging` plugin (debug only) |

Ktor was chosen over Retrofit/OkHttp-direct for two reasons: native Kotlin coroutines integration
(suspend functions on the call sites without adapters) and the built-in `Auth` plugin's automatic
challenge-and-refresh flow, which removes a layer of boilerplate around OAuth token rotation.

## Two clients, one provider

`HttpClientProvider` exposes two factories:

```kotlin
fun createUnauthenticated(isDebug: Boolean): HttpClient
fun createAuthenticated(
    isDebug: Boolean,
    loadTokens: suspend () -> BearerTokens?,
    refreshTokens: suspend (refreshToken: String) -> BearerTokens?,
): HttpClient
```

Two clients are required because of an OAuth chicken-and-egg: refreshing the access token is
itself an HTTP call, and that call **must not** go through the same `Auth` plugin. If it did, a
401 on the refresh endpoint would recursively trigger another refresh — infinite loop. So:

| Client | Used for | DI binding |
|---|---|---|
| Unauthenticated | `/oauth/token` (code exchange + refresh) | `@Provides fun provideHttpClient()` |
| Authenticated | `/uploads`, `/activities/{id}`, etc. | `@Provides @StravaAuthenticatedClient fun provideAuthenticatedHttpClient(...)` |

The unauthenticated client signs OAuth requests with form-encoded `client_id` /
`client_secret` / `code` per Strava's spec. The authenticated client attaches `Authorization:
Bearer …` and handles refresh transparently.

## Auth plugin behavior

The authenticated client installs Ktor's `Auth` plugin with the `bearer` scheme:

```kotlin
install(Auth) {
    bearer {
        loadTokens { … }                                   // attach current Bearer
        refreshTokens { … }                                // refresh on 401
        sendWithoutRequest { it.url.host == STRAVA_HOST }  // proactive vs challenge-driven
    }
}
```

### `loadTokens`

Called on every request to fetch the current `BearerTokens`. Backed by
`StravaTokenProvider.loadTokens()` which reads the single token row from Room. Returns `null`
when the user is logged out — in that case the request goes out unsigned and Strava replies 401,
which `StravaErrorMapper` translates to `StravaError.AuthRequired`.

### `refreshTokens`

Called automatically by Ktor when a request comes back 401. Backed by
`StravaTokenProvider.refreshTokens(refreshToken)`. After a successful refresh, Ktor re-fires the
failed request with the new tokens and the caller never sees the original 401.

If the refresh itself fails (network error, refresh token revoked by Strava), the provider
**deletes the local token** before returning `null`. This is critical: without the wipe, a bad
refresh token would loop forever on every authenticated request. With the wipe, the flow falls
back to "logged out" and the user is prompted to re-authorize.

### `sendWithoutRequest` — proactive auth

Without this predicate, Ktor's default `Auth` flow is **challenge-driven** (RFC 7235): the first
request goes out unsigned; if the server responds 401 with `WWW-Authenticate: Bearer`, the
plugin pulls tokens, retries with the header, and only then bubbles the response up. That
doubles the roundtrip count on every request whose tokens haven't been cached by the plugin yet
(its in-memory cache lives only as long as the `HttpClient`).

`sendWithoutRequest = { request.url.host == STRAVA_HOST }` flips the plugin to **proactive**
mode for Strava-host requests: it calls `loadTokens` before dispatching and attaches the Bearer
header up front. One roundtrip instead of two. The Room lookup in `loadTokens` is cheap.

The host predicate has two roles:

1. **Don't leak tokens.** Ktor follows redirects by default. If Strava ever 3xx-redirects to a
   CDN or signed-S3 URL (not currently the case for the endpoints we hit, but defensively
   guarded), the redirected request is a fresh request that goes through the same plugin.
   Without the predicate, our Bearer would attach to that follow-up request, handing a Strava
   credential to an unrelated host.
2. **Skip pointless DB lookups** for non-Strava hosts.

Note that the predicate only controls the *initial* attachment. After a 401, `refreshTokens`
runs and Ktor retries with new tokens regardless of the predicate — token expiry mid-session
recovers transparently.

## Common config

Both clients share `commonConfig`:

| Setting | Reason |
|---|---|
| `ContentNegotiation { json(ignoreUnknownKeys = true, explicitNulls = false) }` | Strava silently adds fields; our DTOs tolerate missing keys |
| `Logging { level = if (isDebug) HEADERS else NONE }` | Debug-only — release never prints Bearer tokens to logcat |
| `expectSuccess = true` | Non-2xx throws `ClientRequestException` / `ServerResponseException`; centralized mapping in `StravaErrorMapper` instead of per-call status checks |

## Error mapping

`StravaErrorMapper` translates Ktor exceptions into the internal `StravaError` sealed-class
hierarchy:

| Throwable | `StravaError` | `SyncErrorReason` | `isRetryable` |
|---|---|---|---|
| `IOException`, `ConnectTimeoutException`, `SocketTimeoutException` | `Network` | `NETWORK` | `true` |
| `ClientRequestException` 401 / 403 | `AuthRequired` | `AUTH_REQUIRED` | `false` |
| `ClientRequestException` 429 | `RateLimited(retryAfter)` | `RATE_LIMITED` | `true` |
| `ClientRequestException` 400 / 422 | `InvalidActivity` | `INVALID_ACTIVITY` | `false` |
| `ServerResponseException` (5xx) | `Server` | `SERVER` | `true` |
| anything else | `Unknown` | `UNKNOWN` | `false` |

`StravaError` is a sealed class extending `Throwable` (so it works with Kotlin `Result<T>`),
with **regular `class` subtypes** rather than `data object`. Each `throw` therefore captures a
fresh stack trace — singleton `Throwable`s would freeze the trace at first construction and
mislead any future debugging.

The 429 mapper parses Strava's `Retry-After` header into a `kotlin.time.Duration`, currently
unused by callers but ready for adaptive backoff if rate-limiting becomes a problem.

## Token lifecycle

```
StravaOAuthCodeProcessor.process(code)
        │  HTTP POST /oauth/token (unauthenticated client)
        ▼
StravaTokenLocalDataSource.upsert  ◄──┐
                                      │
StravaTokenProvider.loadTokens ───────┘  read by Auth plugin on every request

… 401 from Strava …
        │
        ▼
StravaTokenProvider.refreshTokens
        │  HTTP POST /oauth/token (unauthenticated client)
        │  with grant_type=refresh_token
        ├─ success → upsert new tokens, retry original request
        └─ failure → delete local token, surface AuthRequired upstream
```

Tokens are stored in the same SQLCipher-encrypted Room database as everything else — see
[Security](security.md). The token row uses `SINGLETON_ID = 0` (only one Strava account at a
time).

## Testability

The Ktor `HttpClient` configuration itself is not unit-tested directly — the interesting
behavior lives in classes that the client delegates to:

- `StravaTokenProvider` — `loadTokens` mapping, `refreshTokens` happy/sad paths, local-wipe on
  failure (`StravaTokenProviderTest`).
- `StravaErrorMapper` — full status-code matrix using `MockEngine` from Ktor's test utilities
  (`StravaErrorMapperImplTest`).

The client itself is exercised end-to-end via the use-case-level tests with mocked repositories
and remote data sources.
