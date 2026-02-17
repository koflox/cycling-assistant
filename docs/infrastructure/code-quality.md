# Code Quality

## Detekt

The project enforces zero Detekt issues with auto-correct enabled.

```bash
./gradlew detektRun
```

Key rules:

- **ReturnCount** — max 2 explicit returns per function. Use `if/else` or `when` to avoid multiple early returns.
- **Max line length** — 150 characters
- **Trailing commas** — required on declarations

## Kover

Code coverage is generated via Kover and reported on PRs by the CI pipeline.

```bash
./gradlew koverXmlReport
```

Coverage thresholds for badge coloring:

| Coverage   | Color  |
|------------|--------|
| >= 60%     | Green  |
| >= 30%     | Yellow |
| < 30%      | Red    |

## Code Conventions

### Naming

- **Boolean properties** — prefix with `is`, `has`, `are`
- **Companion object** — place at the top of the class body
- **String resources** — follow `feature_component_description` pattern (e.g., `session_stat_*`, `notification_*`, `dialog_*`)

### Visibility Modifiers

| Element                                          | Visibility   |
|--------------------------------------------------|--------------|
| Domain use case interfaces                       | `public` if cross-module, `internal` if module-local |
| Domain repository interfaces                     | `public`     |
| Data layer interfaces (DataSource, Mapper)       | `internal`   |
| All `*Impl` classes                              | `internal`   |
| All ViewModels                                   | `internal`   |
| Top-level feature Koin modules                   | `public`     |
| Sub-module Koin modules (`domainModule`, etc.)   | `internal`   |
| Private DI sub-modules (`dataModule`, etc.)      | `private`    |

### Composable Conventions

| Layer          | Naming Pattern  | Visibility | ViewModel        |
|----------------|-----------------|------------|------------------|
| Entry point    | `<Name>Route`   | `internal` | Obtained via DI  |
| Screen content | `<Name>Content` | `private`  | Passed as params |

- Only expose publicly required params (e.g., `onBackClick`, `onNavigateTo...`)
- Content-level composables should have previews for all their states
- No blank lines between composable body elements

### Build Conventions

- All plugins use version catalog aliases: `alias(libs.plugins.*)`
- Root `build.gradle.kts` sets shared Android config via `subprojects {}` (compileSdk, minSdk, Java 11, JVM target) — feature modules only set `namespace`
- Modules in `settings.gradle.kts` are alphabetically sorted

