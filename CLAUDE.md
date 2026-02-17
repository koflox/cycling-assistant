# CLAUDE.md

Guidance for Claude Code when working with this repository.

## Project Overview

CyclingAssistant is an Android app built with Jetpack Compose that helps cyclists discover cycling
destinations. Features include destination selection, session tracking with foreground service, and
real-time location updates.

## Build Commands

```bash
./gradlew detektRun    # Lint & auto-format (run max 2 times)
./gradlew build        # Build
./gradlew test         # Unit tests
./gradlew installDebug # Install on device
```

## Architecture

### Module Structure

```
CyclingAssistant/
├── app/                              # Shell - navigation, theme, Koin bootstrap, Room DB
├── feature/
│   ├── bridge/                       # Cross-feature communication (alphabetical pair names)
│   │   ├── destination-nutrition/    # destinations ↔ nutrition
│   │   ├── destination-session/      # destinations ↔ session
│   │   ├── nutrition-session/        # nutrition ↔ session
│   │   ├── nutrition-settings/       # nutrition ↔ settings
│   │   └── profile-session/          # profile ↔ session
│   │       ├── api/                  # Interfaces exposed to consumers
│   │       └── impl/                 # Implementations wiring to provider internals
│   ├── dashboard/                    # Main dashboard with expandable menu
│   ├── destinations/                 # Destination selection feature
│   ├── nutrition/                    # Nutrition tracking and reminders
│   ├── profile/                      # Rider profile management
│   ├── session/                      # Session tracking with foreground service
│   └── settings/                     # App settings (theme, language)
└── shared/
    ├── concurrent/                   # Coroutine dispatchers, suspendRunCatching
    ├── design-system/                # UI theme, colors, spacing, components
    ├── di/                           # Koin qualifiers
    ├── distance/                     # Distance calculator
    ├── error/                        # Error mapping utilities
    ├── graphics/                     # Bitmap utilities
    ├── id/                           # ID generator
    ├── location/                     # Location services
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
`session-destination`).

```
feature:destinations ──depends on──> feature:bridge:destination-session:api
                                            │
                                            │ (interface)
                                            ▼
                                     CyclingSessionUseCase
                                            │
                                            │ (implemented by)
                                            ▼
feature:session <──depends on── feature:bridge:destination-session:impl
```

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

### Feature: Destinations

Handles destination discovery and selection. Clean Architecture with di/, domain/, data/,
presentation/ layers.

**Key Use Cases:**

- `GetDestinationInfoUseCase` - Get random destinations or by specific ID
- `GetUserLocationUseCase` - Get current user location
- `InitializeDatabaseUseCase` - Seed database on first launch

**App Recovery Flow:**
When app restarts with active session, `DestinationsViewModel`:

1. Checks for active session via `CyclingSessionUseCase.getActiveSessionDestination()`
2. Fetches destination by ID via `GetDestinationInfoUseCase.getDestinations()`
3. Restores UI state including slider position

### Feature: Session

Manages cycling session lifecycle with foreground service for background tracking.

**Components:**

- `SessionTrackingService` - Foreground service with `START_STICKY`
- `SessionNotificationManager` - Notification with pause/resume/stop actions
- `SessionServiceController` - Interface to start/stop service from ViewModel

**Use Cases:**

- `ActiveSessionUseCase` - Observe/get active session
- `CreateSessionUseCase` - Create new session
- `UpdateSessionStatusUseCase` - Pause/resume/stop
- `UpdateSessionLocationUseCase` - Add track points

**Service Flow:**

```
SessionViewModel
    │ starts service on session create
    ▼
SessionTrackingService (foreground, type=location)
    ├── Observes ActiveSessionUseCase
    ├── Collects location every 3s
    ├── Updates notification every 1s
    └── Handles notification actions
```

### Database

Centralized Room database in app module (`AppDatabase`):

- `DestinationDao` - Cycling POI data
- `SessionDao` - Session and track points

**DAO conventions:** `@Dao` interfaces with suspend functions for one-shot operations and `Flow`
for observable queries. Default conflict strategy: `OnConflictStrategy.REPLACE`.

### State Management (MVVM)

ViewModels expose two flows: `StateFlow<UiState>` for persistent UI state and
`Flow<Navigation>` via Channel for one-time navigation events.

**ViewModel Structure:**

```kotlin
internal class FeatureViewModel(
    private val useCase: UseCaseType,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<FeatureNavigation>()
    val navigation = _navigation.receiveAsFlow()

    init { initialize() }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) { loadData() }
    }

    fun onEvent(event: FeatureUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is FeatureUiEvent.Action -> handleAction()
            }
        }
    }

    private inline fun updateContent(transform: (FeatureUiState.Content) -> FeatureUiState.Content) {
        val current = _uiState.value
        if (current is FeatureUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
```

**UiState** — sealed interface with explicit states, no meaningless defaults:

```kotlin
internal sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    data class Content(val data: DataType, val overlay: Overlay? = null) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}

internal sealed interface Overlay { /* Dialog, Processing, Ready, Error variants */ }
```

**UiEvent** — sealed interface, one per feature. `data object` for parameterless events,
`data class` for parameterized. Handler methods are private.

**Navigation** — sealed interface, emitted via `_navigation.send(...)`, collected in Route with
`LaunchedEffect(Unit)`.

**Screen Pattern:**

```kotlin
@Composable
internal fun FeatureRoute(
    onNavigateToDashboard: () -> Unit,
    viewModel: FeatureViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                FeatureNavigation.ToDashboard -> onNavigateToDashboard()
            }
        }
    }
    FeatureContent(uiState = uiState, onEvent = viewModel::onEvent)
}
```

**Key Rules:**

- Use `Overlay` sealed interface for dialogs/toasts within `Content` state
- Inject `dispatcherDefault` via DI (`DispatchersQualifier.Default`)
- `init` calls `initialize()` which uses `launch(dispatcherDefault)`
- `onEvent` always wraps handling in `launch(dispatcherDefault)`
- Use `updateContent` helper for partial updates within Content state

**Reference:** `SessionCompletionViewModel`, `SessionCompletionUiState`,
`SessionCompletionNavigation`

## Code Conventions

- **Detekt**: Zero issues tolerance, auto-correct enabled
- **Trailing Commas**: Required on declarations
- **Max Line Length**: 150 characters
- **Companion Object**: Place at the top of the class body
- **Boolean Props**: Prefix with `is`, `has`, `are`
- **Dispatchers**: Inject via `DispatchersQualifier`, never hardcode

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

### Visibility Modifiers

| Element                        | Visibility |
|--------------------------------|------------|
| Domain use case interfaces     | `public` if cross-module, `internal` if module-local |
| Domain repository interfaces   | `public`   |
| Data layer interfaces (DataSource, Mapper) | `internal` |
| All `*Impl` classes            | `internal` |
| All ViewModels                 | `internal` |
| Top-level feature Koin modules | `public`   |
| Sub-module Koin modules (`domainModule`, `presentationModule`) | `internal` |
| Private DI sub-modules (`dataModule`, `dataSourceModule`, `repoModule`) | `private` |

### DI (Koin)

**Registration conventions:**

| Type       | Scope     |
|------------|-----------|
| UseCase    | `factory` (`single` if stateful — e.g. internal buffer/smoother) |
| DataSource | `single`  |
| Mapper     | `single`  |
| Repository | `single`  |
| ViewModel  | `viewModel { }` |

**Feature DI file organization:**

- `DataModule.kt` — `private val dataModule`, `private val dataSourceModule`,
  `private val repoModule`, exported as `internal val dataModules: List<Module>`
- `DomainModule.kt` — `internal val domainModule`
- `PresentationModule.kt` — `internal val presentationModule`
- `<Feature>Module.kt` — public aggregator: `val featureModule = module { includes(...) }`

### Design System

Use constants from `shared/design-system/theme/Spacing.kt`: `Spacing.*`, `Elevation.*`,
`CornerRadius.*`, `SurfaceAlpha.*`. Theme state via `LocalDarkTheme.current`. Color schemes:
`CyclingLightColorScheme` / `CyclingDarkColorScheme` (Material 3).

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
constants are defined in feature navigation files.

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

### Build Conventions

- All plugins use version catalog aliases: `alias(libs.plugins.*)`
- Root `build.gradle.kts` sets shared Android config via `subprojects {}` (compileSdk, minSdk,
  Java 11, JVM target) — feature modules do NOT repeat these, only set `namespace`
- Modules in `settings.gradle.kts` are alphabetically sorted

## Unit Testing

### Test Structure

Tests in `src/test/java` per module. Naming: `<ClassName>Test.kt`.

**Dependencies:** JUnit 4, MockK, Turbine, kotlinx-coroutines-test, `shared:testing`

### ViewModel Test Pattern

```kotlin
class FeatureViewModelTest {

    companion object {
        private const val TEST_ID = "test-123"
        private const val ERROR_MESSAGE = "Something went wrong"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase: FeatureUseCase = mockk()
    private val errorMapper: ErrorMessageMapper = mockk()
    private lateinit var viewModel: FeatureViewModel

    @Before
    fun setup() { setupDefaultMocks() }

    private fun setupDefaultMocks() {
        coEvery { errorMapper.map(any()) } returns ERROR_MESSAGE
    }

    private fun createViewModel() = FeatureViewModel(
        useCase = useCase,
        errorMapper = errorMapper,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { useCase.getData() } returns Result.success(createData())
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is FeatureUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Testing Conventions

- `companion object` for test constants at the top
- `@get:Rule` for `MainDispatcherRule`
- Mocks as class properties, `@Before` for common mock setup
- `createViewModel()` factory method for consistent initialization
- No region comments — test names should be self-documenting
- Backtick names: `` `action/condition expected result` ``

**Common Patterns:**

| Pattern                            | Usage                                      |
|------------------------------------|--------------------------------------------|
| `coEvery { ... }`                  | Mock suspend functions                     |
| `every { ... }`                    | Mock regular functions                     |
| `coVerify { ... }`                 | Verify suspend function calls              |
| `slot<T>()`                        | Capture arguments for verification         |
| `awaitItem()`                      | Wait for next Flow emission (Turbine)      |
| `cancelAndIgnoreRemainingEvents()` | End test without consuming remaining items |

### Test Factory Functions

| Scope        | Location                                | Consumed via                            |
|--------------|-----------------------------------------|-----------------------------------------|
| Cross-module | `src/testFixtures/kotlin/.../testutil/` | `testImplementation(testFixtures(...))` |
| Module-local | `src/test/java/.../testutil/`           | Direct import                           |

**Rules:**

- All parameters have empty/zero defaults (empty strings, `0`, `0.0`, `emptyList()`, etc.)
- Tests pass explicit values using constants from `companion object` — no inline magic values
- Use `testFixtures` for cross-module sharing (enable in `build.gradle.kts` with
  `android { testFixtures { enable = true } }`)

**Reference:** `SessionTestFactories.kt`, `DestinationTestFactories.kt`,
`SessionCompletionViewModelTest`

## Localization

Supported languages: English (default), Russian (`values-ru`), Japanese (`values-ja`).

- All user-facing strings in `res/values/strings.xml`, never hardcoded
- Always add translations to all supported locales when adding/modifying strings

## Adding New Features

1. Create module under `feature/<name>/` with di/, domain/, data/, presentation/
2. Add to `settings.gradle.kts` (alphabetically sorted)
3. Export DI module and include in `app/Modules.kt`
4. Add navigation in `AppNavHost.kt`

**For cross-feature communication:**

1. Create bridge under `feature/bridge/<A-B>/` (A and B in alphabetical order) with `api/` and
   `impl/` submodules
2. Consumer depends on API, impl depends on provider
3. DI module name follows `<aB>BridgeImplModule` pattern (e.g., `destinationSessionBridgeImplModule`)

## Key Files

| Path                                                                            | Purpose                   |
|---------------------------------------------------------------------------------|---------------------------|
| `app/navigation/AppNavHost.kt`                                                  | Central navigation wiring |
| `app/Modules.kt`                                                                | Root DI configuration     |
| `app/data/AppDatabase.kt`                                                       | Room database             |
| `feature/session/service/SessionTrackingService.kt`                             | Foreground service        |
| `feature/theme/domain/usecase/ObserveThemeUseCase.kt`                           | Theme observation         |
| `feature/bridge/destination-session/api/.../CyclingSessionUseCase.kt`           | Bridge data interface     |
| `feature/bridge/destination-session/api/.../CyclingSessionUiNavigator.kt`       | Bridge UI interface       |
| `shared/concurrent/.../SuspendRunCatching.kt`                                   | Coroutine-safe runCatching |

## API Keys

Configure Google Maps in `secrets.properties`:

```
MAPS_API_KEY=your_key_here
```
