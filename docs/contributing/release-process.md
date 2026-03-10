# Release Process

Step-by-step guide for releasing a new version.

## Prerequisites

GitHub repository secrets must be configured:

| Secret | Purpose |
|--------|---------|
| `GOOGLE_SERVICES_JSON` | Base64-encoded `google-services.json` |
| `MAPS_API_KEY` | Google Maps API key |
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release keystore (`.jks`) |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Signing key alias |
| `RELEASE_KEY_PASSWORD` | Signing key password |
| `GIST_TOKEN` | Personal access token for badge updates |

These secrets are consumed by the [Setup Secrets](../infrastructure/ci-cd.md#setup-secrets-setup-secrets) reusable action.

## Steps

1. **Develop on `staging`** — all feature branches merge into `staging` via PRs.

2. **Bump version** — update `version.properties` on the release branch:
    ```properties
    versionCode=16       # increment by exactly 1
    versionName=1.9.0    # bump at least one semver part
    ```

3. **Regenerate baseline profiles** (recommended when critical user journeys changed):
    ```bash
    ./gradlew :app:generateReleaseBaselineProfile
    ```
    Requires a device or emulator (API 28+). Commit the generated files in `app/src/release/generated/baselineProfiles/`. Stale profiles still work (ART ignores removed methods) but won't cover new hot paths. See [Performance — Generating Profiles](../infrastructure/performance.md#generating-profiles) for details.

4. **Create PR `staging → main`** — this triggers:
    - [Unit Tests](../infrastructure/ci-cd.md#unit-tests) — detekt + tests + coverage comment
    - [Version Check](../infrastructure/ci-cd.md#version-check) — validates version bump
    - [Baseline Profile Verification](../infrastructure/ci-cd.md#baseline-profile-verification) — AAB integrity + PR comment

5. **Merge to `main`** — this triggers:
    - [Build & Release](../infrastructure/ci-cd.md#build--release) — builds signed APK, creates GitHub release with tag `v<versionName>`
    - [Project Stats](../infrastructure/ci-cd.md#project-stats) — updates badges
    - [Update Module Graph](../infrastructure/ci-cd.md#update-module-graph) — commits graph if changed
    - [Deploy Docs](../infrastructure/ci-cd.md#deploy-docs) — publishes docs site if docs changed

6. **Verify** — check that the GitHub release was created with the correct tag and APK.

## Commit message

Use the `release` prefix for the version bump commit:

```
release: 1.9.0
```

See [Commit Messages](commit-messages.md) for all prefixes.
