# CLAUDE.md

Guidance for Claude Code when working with this repository.

## Detailed Documentation

Full architecture docs, feature guides, and infrastructure details: [docs site](https://koflox.github.io/cycling-assistant/) or browse `docs/` locally.

## Project Overview

CyclingAssistant is an Android app built with Jetpack Compose that helps cyclists discover cycling
destinations, track sessions with a foreground service, connect BLE power meters for real-time
wattage and cadence, and customize session statistics display. Features include destination
selection, session tracking with real-time location updates, power meter integration, configurable
stat items, nutrition tracking, and POI management.

## Build Commands

```bash
./gradlew detektRun                  # Lint & auto-format (run max 2 times)
./gradlew build                      # Build
./gradlew test                       # Unit tests
./gradlew connectedDebugAndroidTest  # UI tests (requires device/emulator)
./gradlew installDebug               # Install on device
```

## Architecture

### Module Structure

See also: [Module Dependency Graph](docs/MODULE_GRAPH.md) | [Module Structure docs](docs/architecture/module-structure.md)

```
CyclingAssistant/
├── app/                              # Shell - navigation, theme, Hilt bootstrap, Room DB
├── build-logic/                      # Convention plugins (cycling.feature, cycling.library, etc.)
├── feature/
│   ├── bridge/                       # Cross-feature communication (alphabetical pair names)
│   │   ├── connection-session/       # connections ↔ session
│   │   ├── destination-nutrition/    # destinations ↔ nutrition
│   │   ├── destination-poi/          # destinations ↔ poi
│   │   ├── destination-session/      # destinations ↔ session
│   │   ├── nutrition-session/        # nutrition ↔ session
│   │   ├── nutrition-settings/       # nutrition ↔ settings
│   │   ├── poi-settings/             # poi ↔ settings
│   │   ├── profile-session/          # profile ↔ session
│   │   └── session-settings/         # session ↔ settings
│   │       ├── api/                  # Interfaces exposed to consumers
│   │       └── impl/                 # Implementations wiring to provider internals
│   ├── connections/                  # BLE device connection and management
│   ├── dashboard/                    # Main dashboard with expandable menu
│   ├── destinations/                 # Destination selection feature
│   ├── locale/                       # App language persistence and observation
│   ├── nutrition/                    # Nutrition tracking and reminders
│   ├── poi/                          # POI type selection and active session POI actions
│   ├── profile/                      # Rider profile management
│   ├── sensor/
│   │   └── power/                    # Power meter test mode and observation
│   ├── session/                      # Session tracking, stats display config
│   ├── settings/                     # App settings (theme, language, stats, POI)
│   └── theme/                        # App theme persistence and observation
└── shared/
    ├── altitude/                     # Altitude gain calculator
    ├── ble/                          # BLE primitives (GATT, scanning, permissions)
    ├── concurrent/                   # Coroutine dispatchers, suspendRunCatching, ConcurrentFactory
    ├── design-system/                # UI theme, colors, spacing, components
    ├── di/                           # Hilt qualifier annotations
    ├── distance/                     # Distance calculator
    ├── error/                        # Error mapping utilities
    ├── graphics/                     # Bitmap utilities
    ├── id/                           # ID generator
    ├── location/                     # Location services
    ├── map/                          # Google Maps route rendering constants & utilities
    ├── sensor-protocol/              # BLE sensor data parsing (cycling power)
    └── testing/                      # Test utilities
```

### Layer Dependencies

Each feature follows Clean Architecture with strict unidirectional dependencies:

```
Composable (Route/Content)
    │ observes
    ▼
ViewModel
    │ calls
    ▼
UseCase
    │ calls
    ▼
Repository
    │ calls
    ▼
DataSource
```

**Rules:**

- Upper layers depend on lower layers, never the reverse
- `presentation` → `domain` → `data` (domain has no Android dependencies)
- UseCase, DataSource, Mapper, and Repository follow Interface + Impl pattern
- UseCase interface and Impl are colocated in the same file; related custom exceptions live there
  too
- ViewModel never accesses Repository or DataSource directly — always through UseCase
- DataSource never accesses UseCase or ViewModel

### Cross-Feature Communication (Bridge Pattern)

Features communicate via bridge modules under `feature/bridge/`. Each bridge is named as an
alphabetically-ordered pair of the two features it connects (e.g., `destination-session`, not
`session-destination`). See [Bridge Pattern docs](docs/architecture/bridge-pattern.md) for full
list and DI names.

When feature A needs to display UI from feature B, use callback-based bridge interfaces (no
`NavController` in bridge APIs):

```kotlin
// Bridge API - uses callbacks, not NavController
interface CyclingSessionUiNavigator {
    @Composable
    fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
        onNavigateToCompletion: (sessionId: String) -> Unit,
    )
}
```

Bridge interface composables use descriptive names (`SessionScreen`, `DestinationOptions`,
`StartFreeRoamSession`); feature implementations use `<Name>Route` (`SessionScreenRoute`,
`DestinationOptionsRoute`, `FreeRoamSessionRoute`).

### Database

Centralized Room database in app module (`AppDatabase`) with `DestinationDao`, `LocaleDao`,
`PairedDeviceDao`, `ProfileDao`, `SessionDao`. DAO conventions and ConcurrentFactory-based access
described in [Architecture Overview](docs/architecture/overview.md) and
[Concurrency docs](docs/infrastructure/concurrency.md).

### Security

SQLCipher encrypts the Room database in release builds. See
[Security docs](docs/infrastructure/security.md) for details.

### State Management (MVVM)

ViewModels expose `StateFlow<UiState>` + `Flow<Navigation>` via Channel. Key rules:

- Use `Overlay` sealed interface for dialogs/toasts within `Content` state
- Inject `dispatcherDefault` via DI (`@DefaultDispatcher`)
- `init` calls `initialize()` which uses `launch(dispatcherDefault)`
- `onEvent` always wraps handling in `launch(dispatcherDefault)`
- Use `updateContent` helper for partial updates within Content state

**Reference:** `SessionCompletionViewModel`, `SessionCompletionUiState`,
`SessionCompletionNavigation`. Full patterns in
[State Management docs](docs/architecture/state-management.md).

## Code Conventions

- **Detekt**: Zero issues tolerance, auto-correct enabled
- **Trailing Commas**: Required on declarations
- **Max Line Length**: 150 characters
- **Companion Object**: Place at the top of the class body
- **Boolean Props**: Prefix with `is`, `has`, `are`
- **Dispatchers**: Inject via `@IoDispatcher`/`@DefaultDispatcher`/`@MainDispatcher` qualifiers, never hardcode

### Interface + Impl Pattern

UseCases, DataSources, Mappers, and Repositories all follow the Interface + Impl pattern.
UseCase interface and `Impl` are colocated in the same file along with related custom exceptions.
Mapper and DataSource interfaces may use separate files for interface and impl.

### Error Handling

Use `suspendRunCatching` (from `shared/concurrent`) instead of `runCatching` in coroutine contexts.
It rethrows `CancellationException` to preserve structured concurrency. Used consistently in
repositories and use cases.

Repository suspend functions return `Result<T>`. Observable functions return `Flow<T>` (no `Result`
wrapping for flows).

### Dispatcher Usage by Layer

| Layer      | Dispatcher          |
|------------|---------------------|
| ViewModel  | `dispatcherDefault` |
| Mapper     | `dispatcherDefault` |
| Repository | delegates (or `dispatcherDefault`) |
| DataSource | `dispatcherIo`      |

**Flow threading rule:** DataSource and IO-bound UseCase flows must have `.flowOn(dispatcherIo)` at
the source. This ensures collectors (VMs on Default) never accidentally run IO work on the wrong
dispatcher.

### Temporal Constants

Use `kotlin.time.Duration` for all temporal constants (timeouts, intervals, delays, durations).
`const val` is not supported for `Duration` (inline class), use `val` instead.

```kotlin
// ✓ Good
private val SCAN_TIMEOUT = 30.seconds
private val DEBOUNCE_INTERVAL = 400.milliseconds
delay(SCAN_TIMEOUT)
locationRequest.intervalMs = SCAN_TIMEOUT.inWholeMilliseconds  // when API requires Long

// ✗ Bad
private const val SCAN_TIMEOUT_MS = 30_000L
```

**Excluded:** Pure arithmetic conversion factors (`MILLISECONDS_PER_SECOND`, `MINUTES_TO_MS`) remain
`const val Long` — they are math constants, not temporal durations.

### Visibility Modifiers

| Element                        | Visibility |
|--------------------------------|------------|
| Domain use case interfaces     | `public` if cross-module, `internal` if module-local |
| Domain repository interfaces   | `public`   |
| Data layer interfaces (DataSource, Mapper) | `internal` |
| All `*Impl` classes            | `internal` |
| All ViewModels                 | `internal` (`@HiltViewModel internal class ... @Inject constructor`) |
| Hilt `@Module` objects         | `internal` |

### DI (Hilt)

**Scope mapping:**

| Type       | Hilt Pattern |
|------------|-------------|
| UseCase    | `@Provides` (no scope) for stateless; `@Provides @Singleton` for stateful |
| DataSource | `@Provides @Singleton` |
| Mapper     | `@Provides @Singleton` |
| Repository | `@Provides @Singleton` |
| ViewModel  | `@HiltViewModel internal class ... @Inject constructor(...)` |

**Qualifiers:** Defined in `shared/di` as `@Qualifier @Retention(AnnotationRetention.BINARY)`
annotations (e.g., `@IoDispatcher`, `@DefaultDispatcher`, `@SessionDaoFactory`, `@SessionMutex`).

**Composable injection:** Use `hiltViewModel()` for ViewModels. For non-VM dependencies in
Composables, use `@EntryPoint @InstallIn(SingletonComponent::class)` interfaces with
`EntryPointAccessors.fromApplication()`.

**Android components:** Services use `@AndroidEntryPoint` + `@Inject lateinit var`. Activities use
`@AndroidEntryPoint` + `by viewModels()`. Application uses `@HiltAndroidApp`.

**Feature DI file organization:**

- `<Feature>DataHiltModule.kt` — `@Module @InstallIn(SingletonComponent::class) internal object`
- `<Feature>DomainHiltModule.kt` — `@Module @InstallIn(SingletonComponent::class) internal object`
- `<Feature>PresentationHiltModule.kt` — only if non-VM bindings exist (error mappers, factories)
- No aggregator needed — Hilt auto-discovers `@Module` classes

### Design System

Use constants from `shared/design-system/theme/Spacing.kt`: `Spacing.*`, `Elevation.*`,
`CornerRadius.*`, `SurfaceAlpha.*`. Theme state via `LocalDarkTheme.current`. Color schemes:
`CyclingLightColorScheme` / `CyclingDarkColorScheme` (Material 3).

**Click debounce rule:** Use `DebouncedButton` / `DebouncedOutlinedButton` from
`shared/design-system/component/` instead of raw `Button` / `OutlinedButton` to prevent click
spamming (400ms debounce interval).

### Composable Conventions

| Layer          | Naming Pattern  | Visibility | ViewModel        |
|----------------|-----------------|------------|------------------|
| Entry point    | `<Name>Route`   | internal   | Obtained via DI  |
| Screen content | `<Name>Content` | private    | Passed as params |

**Rules:**

- Only expose publicly required params (e.g., `onBackClick`, `onNavigateTo...`)
- Content-level composables should have previews for all their states
- No blank lines between composable body elements

### Navigation

Callback-based pattern — composables are navigation-agnostic. Only `AppNavHost` knows about
`NavController`. Never pass `NavController` to composables.

Feature modules expose `NavGraphBuilder` extension functions with callback parameters. Route
constants are defined in feature navigation files. Features with multiple screens use nested
navigation graphs (e.g., `settingsGraph` wraps settings + POI selection sub-screens).

```kotlin
const val SESSIONS_LIST_ROUTE = "sessions_list"

fun NavGraphBuilder.sessionsListScreen(
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
) {
    composable(route = SESSIONS_LIST_ROUTE) {
        SessionsListRoute(onBackClick = onBackClick, onSessionClick = onSessionClick)
    }
}
```

### String Resource Naming

Follow `feature_component_description` pattern: `session_stat_*`, `session_button_*`,
`notification_*`, `dialog_*`, `permission_*`, `error_*`, `share_*`.

### Build Conventions (Convention Plugins)

See also: [Convention Plugins docs](docs/infrastructure/convention-plugins.md)

Shared build configuration lives in `build-logic/` as precompiled script plugins, replacing the
old `subprojects {}` approach. Each module applies one or more convention plugins instead of
repeating boilerplate.

**Plugin hierarchy:**

| Plugin | What it provides |
|---|---|
| `cycling.library` | Base: `com.android.library` + compileSdk, minSdk, Java 11, kover |
| `cycling.compose` | `kotlin.compose` plugin + Compose BOM + material3 + icons |
| `cycling.hilt` | `ksp` + `hilt` plugins + hilt-android + hilt-compiler |
| `cycling.testing.unit` | junit + mockk + coroutines-test + turbine + shared:testing |
| `cycling.feature` | library + compose + hilt + testing.unit + lifecycle + navigation + coroutines + shared:{concurrent, design-system, di} |
| `cycling.bridge.api` | library (minimal) |
| `cycling.bridge.impl` | library + hilt + testing.unit + coroutines-core |

**Usage in modules:**

```kotlin
// feature/nutrition/build.gradle.kts
plugins {
    id("cycling.feature")
}
android {
    namespace = "com.koflox.nutrition"
}
dependencies {
    // Only module-specific dependencies
}
```

- Modules in `settings.gradle.kts` are alphabetically sorted

## Unit Testing

Tests in `src/test/java` per module. Naming: `<ClassName>Test.kt`. Dependencies: JUnit 4, MockK,
Turbine, kotlinx-coroutines-test, `shared:testing`.

**Key rules:**

- `companion object` for test constants at the top, `@get:Rule` for `MainDispatcherRule`
- Mocks as class properties, `@Before` for common mock setup
- `createViewModel()` factory method for consistent initialization
- Backtick names: `` `action/condition expected result` ``
- Test factory functions: all parameters have empty/zero defaults
- Cross-module factories in `src/testFixtures/kotlin/.../testutil/`, module-local in
  `src/test/java/.../testutil/`

**Reference:** `SessionCompletionViewModelTest`, `SessionTestFactories.kt`,
`DestinationTestFactories.kt`. Full patterns in [Testing docs](docs/infrastructure/testing.md).

## Localization

Supported languages: English (default), Russian (`values-ru`), Japanese (`values-ja`).

- All user-facing strings in `res/values/strings.xml`, never hardcoded
- Always add translations to all supported locales when adding/modifying strings
- **Popup locale rule:** `Dialog`, `AlertDialog`, `DropdownMenu`, and `ExposedDropdownMenu` create
  new windows that don't inherit the app-selected locale from `LocalizedContent`. Always use the
  `Localized*` wrappers from `shared/design-system/component/`:
  - `LocalizedDialog` instead of `Dialog`
  - `LocalizedAlertDialog` instead of `AlertDialog`
  - `LocalizedDropdownMenu` instead of `DropdownMenu`
  - `LocalizedExposedDropdownMenu` instead of `ExposedDropdownMenu`

## Adding New Features

1. Create module under `feature/<name>/` with di/, domain/, data/, presentation/
2. Add to `settings.gradle.kts` (alphabetically sorted)
3. In `build.gradle.kts`: apply `id("cycling.feature")` and set `android.namespace`
   (e.g., `com.koflox.<feature>`). The convention plugin provides Hilt, Compose, testing, and
   common dependencies — only add module-specific ones
4. Create `@Module @InstallIn(SingletonComponent::class)` objects in `di/`
5. Add navigation in `AppNavHost.kt`

**For cross-feature communication:**

1. Create bridge under `feature/bridge/<A-B>/` (A and B in alphabetical order) with `api/` and
   `impl/` submodules
2. API module: apply `id("cycling.bridge.api")` (add `id("cycling.compose")` if exposing
   Composable interfaces)
3. Impl module: apply `id("cycling.bridge.impl")` (add `id("cycling.compose")` if rendering UI)
4. Consumer depends on API, impl depends on provider
5. Bridge impl module: `@Module @InstallIn(SingletonComponent::class) internal object`

## Contribution

### Commit Messages

Format: `<prefix>: <description>`

| Prefix        | Usage                                      |
|---------------|--------------------------------------------|
| `feature`     | New functionality                          |
| `fix`         | Bug fix                                    |
| `ui`          | Visual/UI changes                          |
| `refactoring` | Code restructuring without behavior change |
| `security`    | Security improvements                      |
| `docs`        | Documentation changes                      |
| `cicd`        | CI/CD pipeline changes                     |
| `config`      | Configuration, build, or dependency changes |
| `release`     | Version bump and release prep              |

Examples: `feature: active POI for sessions`, `fix: prevent app crash on location disabling`

## Key Files

| Path                                                                            | Purpose                        |
|---------------------------------------------------------------------------------|--------------------------------|
| `build-logic/src/main/kotlin/cycling.*.gradle.kts`                              | Convention plugins             |
| `app/navigation/AppNavHost.kt`                                                  | Central navigation wiring      |
| `app/di/DatabaseHiltModule.kt`                                                  | Database & app Hilt modules    |
| `app/data/AppDatabase.kt`                                                       | Room database                  |
| `feature/session/service/SessionTrackingService.kt`                             | Foreground service             |
| `feature/session/service/SessionTracker.kt`                                     | Session tracking orchestrator  |
| `feature/connections/.../DeviceListViewModel.kt`                                | BLE device management          |
| `feature/theme/domain/usecase/ObserveThemeUseCase.kt`                           | Theme observation              |
| `feature/bridge/destination-session/api/.../CyclingSessionUseCase.kt`           | Bridge data interface          |
| `feature/bridge/destination-session/api/.../CyclingSessionUiNavigator.kt`       | Bridge UI interface            |
| `feature/bridge/connection-session/api/.../SessionPowerMeterUseCase.kt`         | Power meter bridge interface   |
| `shared/ble/.../BleGattManager.kt`                                              | BLE GATT connection manager    |
| `shared/concurrent/.../SuspendRunCatching.kt`                                   | Coroutine-safe runCatching     |

## API Keys

Configure Google Maps in `secrets.properties`:

```
MAPS_API_KEY=your_key_here
```

Firebase requires `google-services.json` in `app/` (git-ignored). Download from Firebase Console.
