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
│   ├── dashboard/                    # Main dashboard with expandable menu
│   ├── destinations/                 # Destination selection feature
│   ├── session/                      # Session tracking with foreground service
│   ├── settings/                     # App settings (theme, language)
│   └── destination-session/          # Bridge for cross-feature communication
│       ├── bridge/api/               # Interfaces exposed to destinations
│       └── bridge/impl/              # Implementations using session internals
└── shared/
    ├── concurrent/                   # Coroutine dispatchers
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
- UseCase and DataSource follow Interface + Impl pattern (see Code Conventions)
- ViewModel never accesses Repository or DataSource directly — always through UseCase
- DataSource never accesses UseCase or ViewModel

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

| Module          | Purpose                                                |
|-----------------|--------------------------------------------------------|
| `concurrent`    | `DispatchersQualifier.{Io, Main, Default, Unconfined}` |
| `design-system` | Theme, colors, spacing constants, reusable components  |
| `distance`      | `DistanceCalculator` for haversine distance            |
| `error`         | `ErrorMapper` interface for error handling             |
| `graphics`      | `createCircleBitmap()` for map markers                 |
| `id`            | `IdGenerator` for unique IDs                           |
| `location`      | `LocationDataSource` via Play Services                 |

### Design System (`shared/design-system`)

Centralized UI theming and components for consistent styling across the app.

**Structure:**

```
shared/design-system/
├── component/
│   └── FloatingMenuButton.kt    # Reusable floating action button
└── theme/
    ├── Color.kt                 # Color palette (light/dark schemes)
    ├── Spacing.kt               # Spacing, elevation, corner radius constants
    └── Theme.kt                 # Color schemes, LocalDarkTheme
```

**Spacing Constants (`Spacing`):**

| Constant     | Value | Usage                                         |
|--------------|-------|-----------------------------------------------|
| `Tiny`       | 4.dp  | Fine spacing between closely related elements |
| `Small`      | 8.dp  | Button gaps, minor padding                    |
| `Medium`     | 12.dp | Internal card/component padding               |
| `Large`      | 16.dp | Screen edges, cards, containers               |
| `ExtraLarge` | 24.dp | Horizontal dividers, stat item gaps           |
| `Huge`       | 32.dp | Empty/loading states                          |

**Elevation Constants (`Elevation`):**

| Constant    | Value | Usage              |
|-------------|-------|--------------------|
| `Subtle`    | 2.dp  | List items         |
| `Prominent` | 4.dp  | Cards and overlays |

**Corner Radius (`CornerRadius`):**

| Constant | Value | Usage               |
|----------|-------|---------------------|
| `Small`  | 8.dp  | Info windows, chips |
| `Medium` | 12.dp | Buttons, cards      |

**Surface Alpha (`SurfaceAlpha`):**

| Constant   | Value | Usage                      |
|------------|-------|----------------------------|
| `Light`    | 0.9f  | Light overlay transparency |
| `Standard` | 0.95f | Standard overlay           |

**Theme Integration:**

- `LocalDarkTheme`: CompositionLocal providing current dark theme state
- `CyclingLightColorScheme` / `CyclingDarkColorScheme`: Material 3 color schemes
- Access via `LocalDarkTheme.current` in any composable

**Usage Example:**

```kotlin
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.LocalDarkTheme

Card(
    modifier = Modifier.padding(Spacing.Large),
    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
) {
    val isDark = LocalDarkTheme.current
    // ...
}
```

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
├── sessionModule         # Session feature
│   ├── domainModule
│   ├── presentationModule
│   ├── serviceModule
│   └── dataModules
└── settingsModule        # Settings feature (theme, language)
    ├── dataModule
    └── presentationModule
```

### Database

Centralized Room database in app module (`AppDatabase`):

- `DestinationDao` - Cycling POI data
- `SessionDao` - Session and track points

### State Management (MVVM)

ViewModels follow conventions where all work runs on background dispatcher, UI state uses
sealed interfaces for explicit state representation, and navigation is separated into a dedicated
Channel for one-time events.

**Two-Flow Pattern:**

```kotlin
// ViewModel exposes two flows:
// 1. StateFlow<UiState> - persistent UI state
// 2. Flow<Navigation> - one-time navigation events (via Channel)

private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

private val _navigation = Channel<FeatureNavigation>()
val navigation = _navigation.receiveAsFlow()
```

**UiState Convention (Sealed Interface):**

```kotlin
// FeatureUiState.kt - explicit states with no meaningless defaults
internal sealed interface FeatureUiState {
    data object Loading : FeatureUiState

    data class Content(
        val data: DataType,
        val overlay: Overlay? = null,  // For dialogs, toasts, etc.
    ) : FeatureUiState

    data class Error(val message: String) : FeatureUiState
}

// Overlay states for Content (dialogs, sharing, etc.)
internal sealed interface Overlay {
    data object Dialog : Overlay
    data object Processing : Overlay
    data class Ready(val result: ResultType) : Overlay
    data class Error(val message: String) : Overlay
}
```

**Navigation Convention (Sealed Interface + Channel):**

```kotlin
// FeatureNavigation.kt - one-time navigation events
internal sealed interface FeatureNavigation {
    data object ToDashboard : FeatureNavigation
    data class ToDetails(val id: String) : FeatureNavigation
}

// In ViewModel - emit navigation event (consumed once, no reset needed)
_navigation.send(FeatureNavigation.ToDashboard)
```

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

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            loadData()
        }
    }

    fun onEvent(event: FeatureUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is FeatureUiEvent.Action -> handleAction()
            }
        }
    }

    private suspend fun loadData() {
        useCase.getData()
            .onSuccess { data ->
                _uiState.value = FeatureUiState.Content(data = data)
            }
            .onFailure { error ->
                _uiState.value = FeatureUiState.Error(message = error.message)
            }
    }

    private suspend fun navigateToDashboard() {
        _navigation.send(FeatureNavigation.ToDashboard)
    }

    private inline fun updateContent(transform: (FeatureUiState.Content) -> FeatureUiState.Content) {
        val current = _uiState.value
        if (current is FeatureUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
```

**Screen Pattern:**

```kotlin
@Composable
internal fun FeatureRoute(
    onNavigateToDashboard: () -> Unit,
    viewModel: FeatureViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Collect navigation events (one-time, auto-consumed)
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                FeatureNavigation.ToDashboard -> onNavigateToDashboard()
                is FeatureNavigation.ToDetails -> { /* ... */ }
            }
        }
    }

    FeatureContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun FeatureBody(uiState: FeatureUiState) {
    when (uiState) {
        FeatureUiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        is FeatureUiState.Error -> {
            Text(text = uiState.message, modifier = Modifier.align(Alignment.Center))
        }
        is FeatureUiState.Content -> {
            ContentView(data = uiState.data)
        }
    }
}
```

**Key Rules:**

- **Two flows:** `StateFlow<UiState>` for persistent state, `Channel<Navigation>` for one-time
  events
- Use sealed interface for UiState with explicit states (`Loading`, `Content`, `Error`)
- Use sealed interface for Navigation events (consumed once, no reset needed)
- Each state contains only relevant data - no meaningless defaults
- Use `Overlay` sealed interface for dialogs/toasts within `Content` state
- Inject `dispatcherDefault: CoroutineDispatcher` via DI (`DispatchersQualifier.Default`)
- `init` calls `initialize()` which uses `launch(dispatcherDefault)`
- `onEvent` always wraps handling in `launch(dispatcherDefault)`
- Use `updateContent` helper for partial updates within Content state
- Screen collects navigation with `LaunchedEffect(Unit)` for lifecycle-aware collection

**Reference Implementation:** `SessionCompletionViewModel`, `SessionCompletionUiState`,
`SessionCompletionNavigation`

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
internal class FeatureViewModel(
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    fun onEvent(event: FeatureUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                FeatureUiEvent.ButtonClicked -> handleButtonClick()
                is FeatureUiEvent.ItemSelected -> handleItemSelected(event.id)
                FeatureUiEvent.DialogDismissed -> dismissDialog()
            }
        }
    }

    private suspend fun handleButtonClick() { /* ... */ }
    private suspend fun handleItemSelected(id: String) { /* ... */ }
    private fun dismissDialog() { updateContent { it.copy(overlay = null) } }
}

// FeatureScreen.kt - usage in Composable
Button(onClick = { viewModel.onEvent(FeatureUiEvent.ButtonClicked) })
```

**Rules:**

- One `UiEvent` sealed interface per screen/feature
- Use `data object` for events without parameters
- Use `data class` for events with parameters
- Keep event handler methods private in ViewModel
- `onEvent` always launches with `dispatcherDefault`

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

## Unit Testing

### Test Structure

Tests are located in `src/test/java` within each module. Test file naming follows the pattern
`<ClassName>Test.kt`.

**Test Dependencies:**

- JUnit 4 - Test framework
- MockK - Mocking library
- Turbine - Flow testing
- kotlinx-coroutines-test - Coroutine testing
- `shared:testing` - Common test utilities

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
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        coEvery { errorMapper.map(any()) } returns ERROR_MESSAGE
    }

    private fun createViewModel(): FeatureViewModel {
        return FeatureViewModel(
            useCase = useCase,
            errorMapper = errorMapper,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { useCase.getData() } returns Result.success(createData())

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is FeatureUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createData() = Data(id = TEST_ID)
}
```

### Testing Conventions

**Structure:**

- Use `companion object` for test constants at the top
- Use `@get:Rule` for `MainDispatcherRule` (replaces Main dispatcher)
- Declare mocks as class properties
- Use `@Before` for common mock setup
- Create `createViewModel()` factory method for consistent initialization
- No region comments or unnecessary comments between methods - test names should be self-documenting

**Flow Testing with Turbine:**

```kotlin
@Test
fun `loads data and shows Content state`() = runTest {
    val data = createData()
    coEvery { useCase.getData() } returns Result.success(data)

    viewModel = createViewModel()

    viewModel.uiState.test {
        awaitItem() // Loading
        val content = awaitItem() as FeatureUiState.Content
        assertEquals(data, content.data)
    }
}
```

**Navigation Channel Testing:**

```kotlin
@Test
fun `navigates to dashboard on action`() = runTest {
    coEvery { useCase.getData() } returns Result.success(createData())

    viewModel = createViewModel()

    viewModel.navigation.test {
        viewModel.onEvent(FeatureUiEvent.ActionClicked)
        assertEquals(FeatureNavigation.ToDashboard, awaitItem())
    }
}
```

**Testing State Transitions:**

```kotlin
@Test
fun `ShareClicked shows share dialog`() = runTest {
    coEvery { useCase.getData() } returns Result.success(createData())

    viewModel = createViewModel()

    viewModel.uiState.test {
        awaitItem() // Loading
        awaitItem() // Content

        viewModel.onEvent(FeatureUiEvent.ShareClicked)

        val updatedContent = awaitItem() as FeatureUiState.Content
        assertEquals(Overlay.ShareDialog, updatedContent.overlay)
    }
}
```

**Test Naming:**

- Use backticks with descriptive names: `` `loads data and shows Content state` ``
- Format: `<action/condition> <expected result>`

**Common Patterns:**

| Pattern                            | Usage                                      |
|------------------------------------|--------------------------------------------|
| `coEvery { ... }`                  | Mock suspend functions                     |
| `every { ... }`                    | Mock regular functions                     |
| `coVerify { ... }`                 | Verify suspend function calls              |
| `slot<T>()`                        | Capture arguments for verification         |
| `awaitItem()`                      | Wait for next Flow emission (Turbine)      |
| `expectNoEvents()`                 | Assert no more emissions (Turbine)         |
| `cancelAndIgnoreRemainingEvents()` | End test without consuming remaining items |

### Test Factory Functions

Factory functions for creating test objects are organized by sharing scope:

**Location Options:**

| Scope        | Location                                | Consumed via                            |
|--------------|-----------------------------------------|-----------------------------------------|
| Cross-module | `src/testFixtures/kotlin/.../testutil/` | `testImplementation(testFixtures(...))` |
| Module-local | `src/test/java/.../testutil/`           | Direct import                           |

**Test Fixtures (Cross-Module Sharing):**

Use Android's test fixtures feature when factories need to be shared across modules:

```kotlin
// In feature/session/build.gradle.kts
android {
    testFixtures {
        enable = true
    }
}

dependencies {
    // Dependencies needed by testFixtures code
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.ui)
}

// In consuming module's build.gradle.kts
dependencies {
    testImplementation(testFixtures(project(":feature:session")))
}
```

```
feature/session/
├── src/main/
├── src/test/                    # Module's own tests (can access testFixtures)
└── src/testFixtures/kotlin/     # Shared test utilities
    └── com/koflox/session/testutil/
        └── SessionTestFactories.kt
```

**Convention:**

- All parameters have empty/zero defaults (empty strings, `0`, `0.0`, `emptyList()`, etc.)
- For test specific values, tests must pass explicit values using constants defined in
  `companion object`
- No inline magic values - use constants like `SESSION_ID`, `DESTINATION_NAME`, etc.

**Example Factory:**

```kotlin
// In testutil/SessionTestFactories.kt
fun createSession(
    id: String = "",
    destinationId: String = "",
    destinationName: String = "",
    status: SessionStatus = SessionStatus.RUNNING,
    elapsedTimeMs: Long = 0L,
    traveledDistanceKm: Double = 0.0,
    trackPoints: List<TrackPoint> = emptyList(),
) = Session(
    id = id,
    destinationId = destinationId,
    destinationName = destinationName,
    status = status,
    elapsedTimeMs = elapsedTimeMs,
    traveledDistanceKm = traveledDistanceKm,
    trackPoints = trackPoints,
)
```

**Usage in Tests:**

```kotlin
class SessionViewModelTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Test Destination"
    }

    @Test
    fun `loads session and shows Content state`() = runTest {
        // Pass explicit values using constants - no magic values
        val session = createSession(
            id = SESSION_ID,
            destinationName = DESTINATION_NAME,
            status = SessionStatus.COMPLETED,
        )
        coEvery { useCase.getSession(SESSION_ID) } returns Result.success(session)

        // ...
    }
}
```

**Rules:**

- Factory functions use empty/zero defaults to avoid hidden assumptions
- Tests explicitly pass only the values they care about
- Constants are defined in test class `companion object`
- Use `testFixtures` when factories are needed by other modules
- Use `src/test/.../testutil/` when factories are module-local only

**Reference Implementations:**

- `feature/session/src/testFixtures/kotlin/com/koflox/session/testutil/SessionTestFactories.kt`
- `feature/destinations/src/test/java/com/koflox/destinations/testutil/DestinationTestFactories.kt`

**Reference Implementation:** `SessionCompletionViewModelTest`

## Localization

The app supports multiple languages: English (default), Russian (`values-ru`), and
Japanese (`values-ja`).

**Rules:**

- All user-facing strings must be defined in `res/values/strings.xml`, never hardcoded
- When adding or modifying a string resource, always add translations to all supported locales
- String resource files per module: `src/main/res/values/strings.xml`,
  `src/main/res/values-ru/strings.xml`, `src/main/res/values-ja/strings.xml`

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

| Path                                                                      | Purpose                       |
|---------------------------------------------------------------------------|-------------------------------|
| `app/navigation/AppNavHost.kt`                                            | Central navigation wiring     |
| `app/Modules.kt`                                                          | Root DI configuration         |
| `app/data/AppDatabase.kt`                                                 | Room database                 |
| `app/ui/theme/Theme.kt`                                                   | App theme with LocalDarkTheme |
| `feature/destinations/di/DestinationsModule.kt`                           | Destinations DI               |
| `feature/session/di/SessionModule.kt`                                     | Session DI                    |
| `feature/session/navigation/SessionsNavigation.kt`                        | Session routes & functions    |
| `feature/session/service/SessionTrackingService.kt`                       | Foreground service            |
| `feature/settings/di/SettingsModule.kt`                                   | Settings DI                   |
| `feature/settings/api/ThemeProvider.kt`                                   | Theme observation interface   |
| `feature/destination-session/bridge/api/.../CyclingSessionUseCase.kt`     | Bridge data interface         |
| `feature/destination-session/bridge/api/.../CyclingSessionUiNavigator.kt` | Bridge UI interface           |
| `shared/design-system/theme/Color.kt`                                     | Color palette definitions     |
| `shared/design-system/theme/Spacing.kt`                                   | Spacing & dimension constants |
| `shared/design-system/theme/Theme.kt`                                     | Color schemes, LocalDarkTheme |

## API Keys

Configure Google Maps in `secrets.properties`:

```
MAPS_API_KEY=your_key_here
```
