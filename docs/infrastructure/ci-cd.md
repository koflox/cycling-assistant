# CI/CD

CyclingAssistant uses GitHub Actions for continuous integration and delivery, with Dependabot for automated dependency updates.

## Workflows

### PR Checks (orchestrator)

**Trigger:** Pull requests targeting `main` or `staging`, excluding docs-only changes
(`paths-ignore: docs/**`, `mkdocs.yml`, `**/*.md`)

`pr-checks.yml` is a thin orchestrator that runs the per-check **reusable workflows**
(`on: workflow_call`) in two stages, so the heavier Stage 2 checks only start once Stage 1 has
finished:

```
Stage 1 (parallel):  Module Graph  +  Version Check
                              │ (both complete)
                              ▼
Stage 2 (parallel):  Unit Tests  +  Screenshot Tests  +  Baseline Profile Verification
```

Why staged: **Module Graph** may push a regenerated `docs/MODULE_GRAPH.md` commit to the PR
branch. Running the expensive Stage 2 checks first would test a soon-to-be-stale commit (and the
`GITHUB_TOKEN` push deliberately does not re-trigger them). Gating Stage 2 behind Stage 1 — and
checking out `github.head_ref` in Stage 2 — means the heavy checks run once, against the final
commit.

- **Stage 1 is main-only** (`if: github.base_ref == 'main'`); on `staging` PRs both are skipped.
- **Stage 2** uses `needs: [module-graph, version-check]` plus
  `if: ${{ !cancelled() && needs.*.result != 'failure' }}` so it still runs when Stage 1 was
  skipped (staging) but is skipped if Stage 1 failed. Screenshot/Baseline additionally gate on
  `github.base_ref == 'main'`, so `staging` PRs run only Unit Tests — matching the previous behavior.

Each check below keeps its own file as a reusable workflow; the orchestrator owns the trigger,
`concurrency`, and per-job `permissions`. Note: as reusable workflows, the status check names are
reported as `<orchestrator-job> / <job name>` (e.g. `unit-tests / Run Unit Tests`) — update any
branch-protection required-check names accordingly.

### Unit Tests

**Invoked by:** PR Checks (Stage 2). Reusable workflow (`on: workflow_call`).

Runs quality checks and tests on every PR:

- Detekt linting with report upload
- Unit tests with code coverage (Kover)
- Coverage report posted as a PR comment
- Automatic cancellation of previous runs on new commits

### Main Pipeline (orchestrator)

**Trigger:** Push to `main` (merges)

`main-pipeline.yml` orchestrates the post-merge build/release/docs flow as **reusable workflows**
(`on: workflow_call`) in two stages:

```
detect changes ─► Stage 1: Deploy Docs (if docs)  ∥  Build & Cache (if code)
                         └─► Stage 2: Release APK (needs Build & Cache success)
```

- A small **Detect Changes** job diffs `github.event.before..github.sha` and sets `docs` / `code`
  outputs. This preserves the previous path-based behavior that two separate workflows used to
  provide: a docs-only merge deploys docs but does **not** re-release the APK (same version → bogus
  tag); a code-only merge builds + releases but skips the docs deploy.
- **Stage 1** runs in parallel: `deploy-docs` (gated on `docs`), `build-cache` (gated on `code`).
- **Stage 2** `release-apk` uses `needs: [deploy-docs, build-cache]` with
  `if: ${{ !cancelled() && needs.build-cache.result == 'success' }}` — it waits for both but only
  **requires** Build & Cache (so a docs-only merge, where build is skipped, won't release, and a
  Deploy Docs failure won't block a real release). Build & Cache warms the Gradle/build cache that
  Release APK reuses.
- `concurrency: cancel-in-progress: false` — an in-flight release is never cancelled by a newer push.

### Project Stats

**Trigger:** `workflow_run` after **Main Pipeline** completes successfully on `main`, or manual dispatch

Calculates project metrics and updates dynamic badges:

- Code coverage percentage (Kover)
- Lines of Kotlin code
- Module count
- UI component count (screens + standalone dialogs)
- Workflow count

Badges are stored as JSON on a GitHub Gist and rendered via shields.io.

### Build & Cache / Release APK

**Invoked by:** Main Pipeline — `build-cache.yml` as Stage 1 (on code changes), `release-apk.yml` as
Stage 2. Reusable workflows (`on: workflow_call`).

- **Build & Cache** (`build-cache.yml`) — compiles a debug build and saves the Gradle/build cache.
- **Release APK** (`release-apk.yml`) — reads version from `version.properties`, runs `lintRelease`
  (Android Lint, fails on errors), builds signed release APKs (per-ABI splits + universal), creates a
  GitHub release tag, and uploads all APKs. Reuses the cache warmed by Build & Cache.

Lint catches manifest/resource/API-misuse issues before the APK is built. Notably, the `Instantiatable` rule verifies every class referenced from `AndroidManifest.xml` (`<service>`, `<activity>`, `<receiver>`, `<provider>`) actually exists and is instantiatable — this guard would have caught the [PR #8a manifest regression](https://github.com/koflox/cycling-assistant/commit/main) where the moved `SessionTrackingService` package didn't match the new module's namespace. The HTML lint report is uploaded as `lint-report-release` artifact on failure.

`assembleRelease` produces three APKs in `app/build/outputs/apk/release/` thanks to the `splits.abi` block in `app/build.gradle.kts`:

| Output | Target | Approx. size |
|---|---|---|
| `app-arm64-v8a-release.apk` | 64-bit ARM (most modern devices) | ~5.7 MB |
| `app-x86_64-release.apk` | x86_64 (emulators, ChromeOS) | ~5.8 MB |
| `app-universal-release.apk` | Any device — fallback | ~11 MB |

armeabi-v7a (32-bit ARM) and x86 are excluded — virtually no devices left.

All three APKs are uploaded to the GitHub Release (`files: *.apk`) and as a single `app-release-v<name>` workflow artifact.

Main Pipeline's Detect Changes gate prevents redundant rebuilds + duplicate releases on doc-only merges (e.g., the auto-generated module graph update) — `code` is `false`, so Build & Cache and Release APK are skipped.

### Version Check

**Invoked by:** PR Checks (Stage 1, main-only). Reusable workflow (`on: workflow_call`).

Verifies PR version bumps:

- `versionCode` must be incremented by exactly 1
- `versionName` (semantic version) must have at least one part bumped

### Module Graph

**Invoked by:** PR Checks (Stage 1, main-only). Reusable workflow (`on: workflow_call`).

Regenerates `docs/MODULE_GRAPH.md`. If the graph changed, the workflow commits the update **directly into the PR's head branch** (typically `staging`) using `GITHUB_TOKEN`. Because pushes performed via `GITHUB_TOKEN` deliberately do not trigger new workflow runs, the update lands silently in the open PR — no auto-PR, no re-run loop, no separate commit to merge after main.

Sub-features required for this flow:

- `actions/checkout` uses `ref: pull_request.head.ref` to land on the real PR branch (not the virtual merge ref) so push back is possible.
- Other PR workflows ran against the original commit; the bot's docs-only commit on top doesn't re-trigger them. Branch protection rules requiring "up-to-date branches before merging" would conflict with this flow — keep that setting off.

### Screenshot Tests

**Invoked by:** PR Checks (Stage 2, main-only). Reusable workflow (`on: workflow_call`).

Verifies that UI components haven't changed visually by comparing screenshots against committed
golden images using Roborazzi:

- Runs `verifyRoborazziDebug` across all modules with screenshot tests
- On failure: uploads `screenshot-diff` artifact with comparison images and posts a PR comment
- If changes are intentional, developers run `recordRoborazziDebug` locally and commit updated
  golden images

### Baseline Profile Verification

**Invoked by:** PR Checks (Stage 2, main-only). Reusable workflow (`on: workflow_call`).

Builds a release AAB and verifies baseline profile integrity. Results are posted as a PR comment.

| Check | What it verifies |
|-------|-----------------|
| Source profiles committed | `baseline-prof.txt` and `startup-prof.txt` exist and are non-empty |
| Profiles bundled in AAB | `baseline.prof` and `baseline.profm` present in release AAB |
| DEX layout optimization | `r8.json` has `startup=true` only for `classes.dex` |
| SHA-256 validation | DEX checksums match `r8.json` metadata |
| Binary profile size | Warning if compiled profile exceeds 1.5 MB |

See [Performance — CI Verification](performance.md#ci-verification) for technical details.

### Deploy Docs

**Invoked by:** Main Pipeline (Stage 1, on `docs/` or `mkdocs.yml` changes). Also runnable via manual
dispatch (`workflow_dispatch`). Reusable workflow (`on: workflow_call`).

Builds and deploys this documentation site to GitHub Pages using MkDocs Material.

## Reusable Actions

### Setup Android (`setup-android`)

Sets up JDK 17, Gradle, and Android build caching. Optional inputs control whether caches are saved or only restored.

### Setup Secrets (`setup-secrets`)

Creates project secret files and sets release signing environment variables. Inputs:

| Input | Required | Creates |
|-------|----------|---------|
| `google-services-json` | yes | `app/google-services.json` (base64-decoded) |
| `maps-api-key` | no | `MAPS_API_KEY=` line in `secrets.properties` |
| `strava-client-id` | no | `STRAVA_CLIENT_ID=` line in `secrets.properties` (used by `feature:integrations:strava:impl` BuildConfig) |
| `strava-client-secret` | no | `STRAVA_CLIENT_SECRET=` line in `secrets.properties` |
| `release-keystore` | no | `app/release.jks` (base64-decoded) |
| `release-keystore-password` | no | `RELEASE_KEYSTORE_PASSWORD` env var |
| `release-key-alias` | no | `RELEASE_KEY_ALIAS` env var |
| `release-key-password` | no | `RELEASE_KEY_PASSWORD` env var |

All workflows that build the project use this action. Workflows that only need a debug build pass `google-services-json` alone; release builds pass all inputs.

## Future: Affected Module Detection

Currently, the Unit Tests workflow runs **all module tests** on every PR. As the project grows, running only tests for changed modules and their dependents would reduce CI time.

### Why not implemented yet

The most mature solution — [Dropbox AffectedModuleDetector](https://github.com/dropbox/AffectedModuleDetector) — is incompatible with **Gradle 9.x**. It uses the removed `ProjectDependency.getDependencyProject()` API and is in maintenance mode (no new features planned).

### Options to revisit

| Option | Pros | Cons |
|---|---|---|
| **Dropbox AffectedModuleDetector** | Battle-tested, transitive dependency support, `runAffectedUnitTests` task out of the box | Incompatible with Gradle 9+, maintenance mode only |
| **Custom shell script** | No plugin dependency, full control, parses `build.gradle.kts` for `project("...")` + BFS for dependents | Must be maintained manually, fragile if dependency declaration patterns change |
| **Gradle Develocity Predictive Test Selection** | ML-based, test-level granularity | Requires commercial license + infrastructure |
| **Gradle build cache only** | Already in use (`--build-cache`), skips unchanged task outputs | Still configures all modules, no real test skipping on cache miss |

### Recommended trigger

When one of these becomes viable:

- AffectedModuleDetector releases a Gradle 9-compatible version
- A community alternative emerges (e.g., fork with Gradle 9 support)
- CI times grow enough to justify a custom script

## Dependabot

Dependabot creates weekly grouped PRs for:

- Kotlin dependencies
- AndroidX libraries
- Testing libraries
- GitHub Actions versions
