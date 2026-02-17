# State Management

CyclingAssistant uses the MVVM pattern with a structured approach to UI state, events, and navigation.

## ViewModel Structure

Each ViewModel exposes two flows:

- `StateFlow<UiState>` — persistent UI state, observed by the composable
- `Flow<Navigation>` via `Channel` — one-time navigation events

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

    private inline fun updateContent(
        transform: (FeatureUiState.Content) -> FeatureUiState.Content,
    ) {
        val current = _uiState.value
        if (current is FeatureUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
```

### Key Rules

- `init` calls `initialize()` which uses `launch(dispatcherDefault)`
- `onEvent` always wraps handling in `launch(dispatcherDefault)`
- `dispatcherDefault` is injected via DI (`DispatchersQualifier.Default`), never hardcoded
- `updateContent` helper enables partial updates within the `Content` state

## UiState

Sealed interface with explicit states — no meaningless defaults:

```kotlin
internal sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    data class Content(
        val data: DataType,
        val overlay: Overlay? = null,
    ) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}
```

### Overlay Pattern

Dialogs, toasts, and transient UI are modeled as an `Overlay` sealed interface within the `Content` state:

```kotlin
internal sealed interface Overlay {
    // Dialog, Processing, Ready, Error variants
}
```

This keeps dialog state tied to the content that triggered it, avoiding separate state holders.

## UiEvent

Sealed interface with one definition per feature. `data object` for parameterless events, `data class` for parameterized:

```kotlin
internal sealed interface FeatureUiEvent {
    data object Refresh : FeatureUiEvent
    data class SelectItem(val id: String) : FeatureUiEvent
}
```

Handler methods in the ViewModel are always `private`.

## Navigation

Sealed interface emitted via `_navigation.send(...)` and collected in the Route composable with `LaunchedEffect(Unit)`:

```kotlin
internal sealed interface FeatureNavigation {
    data object ToDashboard : FeatureNavigation
    data class ToDetail(val id: String) : FeatureNavigation
}
```

## Screen Pattern

Composables follow a two-layer pattern:

| Layer   | Name              | Visibility | Responsibility            |
|---------|-------------------|------------|---------------------------|
| Route   | `<Name>Route`     | `internal` | Obtains ViewModel via DI, collects state and navigation |
| Content | `<Name>Content`   | `private`  | Pure UI, receives state as params |

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

### Navigation Wiring

Composables are navigation-agnostic. Only `AppNavHost` knows about `NavController`. Feature modules expose `NavGraphBuilder` extension functions with callback parameters:

```kotlin
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
