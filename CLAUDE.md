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
│   ├── destinations/                 # Destination selection feature
│   ├── session/                      # Session tracking with foreground service
│   └── destination-session/          # Bridge for cross-feature communication
│       ├── bridge/api/               # Interfaces exposed to destinations
│       └── bridge/impl/              # Implementations using session internals
└── shared/
    ├── concurrent/                   # Coroutine dispatchers
    ├── di/                           # Koin qualifiers
    ├── distance/                     # Distance calculator
    ├── error/                        # Error mapping utilities
    ├── graphics/                     # Bitmap utilities
    ├── id/                           # ID generator
    ├── location/                     # Location services
    └── testing/                      # Test utilities
```

### Cross-Feature Communication (Bridge Pattern)

Features communicate via bridge modules to maintain separation:

```
feature:destinations ──depends on──> feature:destination-session:bridge:api
                                            │
                                            │ (interface)
                                            ▼
                                     CyclingSessionUseCase
                                            │
                                            │ (implemented by)
                                            ▼
feature:session <──depends on── feature:destination-session:bridge:impl
```

**Bridge API** (`feature/destination-session/bridge/api/`):
```kotlin
interface CyclingSessionUseCase {
    fun observeHasActiveSession(): Flow<Boolean>
    suspend fun getActiveSessionDestination(): ActiveSessionDestination?
}

data class ActiveSessionDestination(val id: String)
```

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

### Shared Modules

| Module       | Purpose                                                |
|--------------|--------------------------------------------------------|
| `concurrent` | `DispatchersQualifier.{Io, Main, Default, Unconfined}` |
| `distance`   | `DistanceCalculator` for haversine distance            |
| `error`      | `ErrorMapper` interface for error handling             |
| `graphics`   | `createCircleBitmap()` for map markers                 |
| `id`         | `IdGenerator` for unique IDs                           |
| `location`   | `LocationDataSource` via Play Services                 |

### DI Hierarchy (Koin)

```
appModule
├── bridgeImplModule      # CyclingSessionUseCase implementation
├── concurrentModule      # Dispatchers
├── databaseModule        # Room DB, DAOs
├── destinationsModule    # Destination feature
├── distanceModule        # DistanceCalculator
├── errorMapperModule     # Error handling
├── idModule              # ID generation
├── locationModule        # Location services
└── sessionModule         # Session feature
    ├── domainModule
    ├── presentationModule
    ├── serviceModule
    └── dataModules
```

### Database

Centralized Room database in app module (`AppDatabase`):

- `DestinationDao` - Cycling POI data
- `SessionDao` - Session and track points

### State Management (MVVM)

```kotlin
class FeatureViewModel(
    private val useCase: UseCaseType,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    fun onEvent(event: FeatureUiEvent) {
        when (event) {
            is FeatureUiEvent.Action -> handleAction()
        }
    }
}
```

### ViewModel Event Pattern

ViewModels expose a single `onEvent(event: UiEvent)` method instead of multiple public methods.
This ensures consistent API and clear separation between UI actions and ViewModel logic.

**Structure:**

```kotlin
// FeatureUiEvent.kt - sealed interface for all UI events
sealed interface FeatureUiEvent {
    data object ButtonClicked : FeatureUiEvent
    data class ItemSelected(val id: String) : FeatureUiEvent
    data object DialogDismissed : FeatureUiEvent
}

// FeatureViewModel.kt - single entry point for events
class FeatureViewModel(...) : ViewModel() {
    fun onEvent(event: FeatureUiEvent) {
        when (event) {
            FeatureUiEvent.ButtonClicked -> handleButtonClick()
            is FeatureUiEvent.ItemSelected -> handleItemSelected(event.id)
            FeatureUiEvent.DialogDismissed -> dismissDialog()
        }
    }

    private fun handleButtonClick() { /* ... */ }
    private fun handleItemSelected(id: String) { /* ... */ }
    private fun dismissDialog() { /* ... */ }
}

// FeatureScreen.kt - usage in Composable
Button(onClick = { viewModel.onEvent(FeatureUiEvent.ButtonClicked) })
```

**Rules:**

- One `UiEvent` sealed interface per screen/feature
- Use `data object` for events without parameters
- Use `data class` for events with parameters
- Keep event handler methods private in ViewModel

## Code Conventions

- **Detekt**: Zero issues tolerance, auto-correct enabled
- **Use Cases**: Interface + Impl pattern, registered as `factory`
- **Data Sources**: Interface + Impl pattern, registered as `single`
- **Dispatchers**: Inject via `DispatchersQualifier`, never hardcode
- **Boolean Props**: Prefix with `is`, `has`, `are`
- **Trailing Commas**: Required on declarations
- **Max Line Length**: 150 characters
- **Companion Object**: Place at the top of the class body

### Composable Conventions

**Structure:**

| Layer          | Naming Pattern  | Visibility | ViewModel        | Example                    |
|----------------|-----------------|------------|------------------|----------------------------|
| Entry point    | `<Name>Route`   | internal   | Obtained via DI  | `SessionCompletionRoute`   |
| Screen content | `<Name>Content` | private    | Passed as params | `SessionCompletionContent` |

**Rules:**

- ViewModels are always marked `internal` since they're obtained within Route composables
- Only expose publicly required params (e.g., `onBackClick`, `onNavigateTo...`)
- Content-level composables should have previews for all their states
- No blank lines between composable body elements

**Navigation:**

Navigation follows a callback-based pattern where composables are navigation-agnostic. Only
`AppNavHost` knows about `NavController`.

**Rules:**

- Never pass `NavController` to composables - use lambda callbacks instead
- Feature modules expose `NavGraphBuilder` extension functions with callback parameters
- Each screen gets its own extension function (one function per composable route)
- Route constants are defined in feature navigation files and used directly in `AppNavHost`

**Feature navigation structure:**

```kotlin
// In feature/session/navigation/SessionsNavigation.kt
const val SESSIONS_LIST_ROUTE = "sessions_list"

fun NavGraphBuilder.sessionsListScreen(
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
) {
    composable(route = SESSIONS_LIST_ROUTE) {
        SessionsListRoute(
            onBackClick = onBackClick,
            onSessionClick = onSessionClick,
        )
    }
}
```

**AppNavHost wiring:**

```kotlin
// In app/navigation/AppNavHost.kt
NavHost(navController = navController, startDestination = DASHBOARD_ROUTE) {
    dashboardScreen(
        onNavigateToSessionsList = { navController.navigate(SESSIONS_LIST_ROUTE) },
        onNavigateToSessionCompletion = { sessionId ->
            navController.navigate(sessionCompletionRoute(sessionId))
        },
    )
    sessionsListScreen(
        onBackClick = { navController.popBackStack() },
        onSessionClick = { sessionId ->
            navController.navigate(sessionCompletionRoute(sessionId))
        },
    )
}
```

**Bridge Pattern (for cross-feature UI):**

When feature A needs to display UI from feature B without direct dependency, use callback-based
bridge interfaces (no `NavController` in bridge APIs):

| Location               | Naming Pattern | Example         |
|------------------------|----------------|-----------------|
| Bridge interface       | `<Name>Screen` | `SessionScreen` |
| Feature implementation | `<Name>Route`  | `SessionRoute`  |

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

// Bridge implementation delegates to feature's internal Route
override fun SessionScreen(
    destinationLocation: Location,
    modifier: Modifier,
    onNavigateToCompletion: (sessionId: String) -> Unit,
) {
    SessionScreenRoute(
        onNavigateToCompletion = onNavigateToCompletion,
        modifier = modifier,
    )
}
```

## Adding New Features

1. Create module under `feature/<name>/` with di/, domain/, data/, presentation/
2. Add to `settings.gradle.kts`
3. Export DI module and include in `app/Modules.kt`
4. Add navigation in `AppNavHost.kt`

**For cross-feature communication:**

1. Create bridge API module with interfaces
2. Create bridge impl module with implementations
3. Consumer depends on API, impl depends on provider

## Key Files

| Path                                                                      | Purpose                    |
|---------------------------------------------------------------------------|----------------------------|
| `app/navigation/AppNavHost.kt`                                            | Central navigation wiring  |
| `app/Modules.kt`                                                          | Root DI configuration      |
| `app/data/AppDatabase.kt`                                                 | Room database              |
| `feature/destinations/di/DestinationsModule.kt`                           | Destinations DI            |
| `feature/session/di/SessionModule.kt`                                     | Session DI                 |
| `feature/session/navigation/SessionsNavigation.kt`                        | Session routes & functions |
| `feature/session/service/SessionTrackingService.kt`                       | Foreground service         |
| `feature/destination-session/bridge/api/.../CyclingSessionUseCase.kt`     | Bridge data interface      |
| `feature/destination-session/bridge/api/.../CyclingSessionUiNavigator.kt` | Bridge UI interface        |

## API Keys

Configure Google Maps in `secrets.properties`:

```
MAPS_API_KEY=your_key_here
```
