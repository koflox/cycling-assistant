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
- Screen count
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

### Deploy Docs

**Trigger:** Push to `main` (when `docs/` or `mkdocs.yml` change), manual dispatch

Builds and deploys this documentation site to GitHub Pages using MkDocs Material.

## Dependabot

Dependabot creates weekly grouped PRs for:

- Kotlin dependencies
- AndroidX libraries
- Testing libraries
- GitHub Actions versions
