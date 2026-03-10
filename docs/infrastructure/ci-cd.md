# CI/CD

CyclingAssistant uses GitHub Actions for continuous integration and delivery, with Dependabot for automated dependency updates.

## Workflows

### Unit Tests

**Trigger:** Pull requests targeting `main` or `staging`

Runs quality checks and tests on every PR:

- Detekt linting with report upload
- Unit tests with code coverage (Kover)
- Coverage report posted as a PR comment
- Automatic cancellation of previous runs on new commits

### Project Stats

**Trigger:** Push to `main`, manual dispatch

Calculates project metrics and updates dynamic badges:

- Code coverage percentage (Kover)
- Lines of Kotlin code
- Module count
- UI component count (screens + standalone dialogs)
- Workflow count

Badges are stored as JSON on a GitHub Gist and rendered via shields.io.

### Build & Release

**Trigger:** Push to `main`

Two-stage pipeline:

1. **Build** — compiles debug build and caches Android build outputs
2. **Release** — reads version from `version.properties`, builds a signed release APK, creates a GitHub release tag, and uploads the APK

### Version Check

**Trigger:** Pull requests targeting `main`

Verifies PR version bumps:

- `versionCode` must be incremented by exactly 1
- `versionName` (semantic version) must have at least one part bumped

### Update Module Graph

**Trigger:** Push to `main`, manual dispatch

Generates the module dependency graph and commits the updated `docs/MODULE_GRAPH.md` if changes are detected.

### Baseline Profile Verification

**Trigger:** Pull requests targeting `main`

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

**Trigger:** Push to `main` (when `docs/` or `mkdocs.yml` change), manual dispatch

Builds and deploys this documentation site to GitHub Pages using MkDocs Material.

## Reusable Actions

### Setup Android (`setup-android`)

Sets up JDK 17, Gradle, and Android build caching. Optional inputs control whether caches are saved or only restored.

### Setup Secrets (`setup-secrets`)

Creates project secret files and sets release signing environment variables. Inputs:

| Input | Required | Creates |
|-------|----------|---------|
| `google-services-json` | yes | `app/google-services.json` (base64-decoded) |
| `maps-api-key` | no | `secrets.properties` |
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
