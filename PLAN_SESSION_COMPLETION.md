# Session Completion Screen Implementation Plan

## Overview

Add a session completion screen that displays session stats and the traveled route on a map. The
screen can be accessed after completing a session (via stop confirmation) or from the sessions list.

## Architecture Decisions

### 1. Map in Session Module

Add Google Maps dependency to session module for displaying the traveled route. This is
session-specific functionality.

### 2. Navigation Flow

```
Active Session → Stop Button → Confirmation Dialog → Yes → Complete Session → Completion Screen
                                                   → No  → Continue Session

Sessions List → Item Click → Completion Screen (for completed sessions only)
```

### 3. Component Reuse

- Extract `SessionStatsDisplay` to be reusable (make public)
- Create `SessionSummaryCard` component (stats without buttons)
- Reuse the card styling from `SessionControlsOverlay`

## Files to Modify/Create

### 1. Dependencies

**`feature/session/build.gradle.kts`**

- Add Google Maps Compose dependency: `libs.maps.compose`
- Add Play Services Maps: `libs.play.services.maps`

### 2. Domain Layer

**`feature/session/src/main/java/com/koflox/session/domain/usecase/GetSessionByIdUseCase.kt`** (NEW)

```kotlin
interface GetSessionByIdUseCase {
    suspend fun getSession(sessionId: String): Result<Session?>
}
```

**`feature/session/src/main/java/com/koflox/session/di/DomainModule.kt`**

- Register `GetSessionByIdUseCase` as factory

### 3. Presentation Layer - Stop Confirmation

**`feature/session/src/main/java/com/koflox/session/presentation/dialog/StopConfirmationDialog.kt`
** (NEW)

```kotlin
@Composable
fun StopConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
)
```

- Title: "Stop Session?"
- Message: "Are you sure you want to stop the current session?"
- Buttons: "Cancel" (dismiss), "Stop" (confirm, error color)

**`feature/session/src/main/java/com/koflox/session/presentation/session/SessionUiState.kt`**

- Add `showStopConfirmationDialog: Boolean = false`

**`feature/session/src/main/java/com/koflox/session/presentation/session/SessionUiEvent.kt`**

- Add `StopConfirmationRequested`, `StopConfirmationDismissed`, `StopConfirmed`

**`feature/session/src/main/java/com/koflox/session/presentation/session/SessionViewModel.kt`**

- Handle new events
- On `StopConfirmed`: complete session and emit navigation event

**`feature/session/src/main/java/com/koflox/session/presentation/session/SessionScreen.kt`**

- Show `StopConfirmationDialog` when `showStopConfirmationDialog` is true

### 4. Presentation Layer - Completion Screen

**
`feature/session/src/main/java/com/koflox/session/presentation/session/components/SessionStatsDisplay.kt`
**

- Change visibility from `internal` to public (for reuse)

**
`feature/session/src/main/java/com/koflox/session/presentation/completion/SessionCompletionUiState.kt`
** (NEW)

```kotlin
data class SessionCompletionUiState(
    val isLoading: Boolean = true,
    val destinationName: String = "",
    val elapsedTimeFormatted: String = "",
    val traveledDistanceFormatted: String = "",
    val averageSpeedFormatted: String = "",
    val topSpeedFormatted: String = "",
    val routePoints: List<LatLng> = emptyList(),
    val startLocation: LatLng? = null,
    val endLocation: LatLng? = null,
    val error: String? = null,
)
```

**
`feature/session/src/main/java/com/koflox/session/presentation/completion/SessionCompletionViewModel.kt`
** (NEW)

```kotlin
class SessionCompletionViewModel(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val sessionUiMapper: SessionUiMapper,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // Load session by ID from savedStateHandle
    // Map track points to LatLng for route display
}
```

**
`feature/session/src/main/java/com/koflox/session/presentation/completion/SessionCompletionScreen.kt`
** (NEW)

```kotlin
@Composable
fun SessionCompletionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Full screen with:
    // - Map filling the screen showing the traveled route (blue polyline)
    // - SessionSummaryCard overlaid at bottom (same style as SessionControlsOverlay but no buttons)
    // - TopAppBar with back button and "Session Complete" title
}
```

**
`feature/session/src/main/java/com/koflox/session/presentation/completion/components/SessionSummaryCard.kt`
** (NEW)

```kotlin
@Composable
fun SessionSummaryCard(
    destinationName: String,
    elapsedTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    modifier: Modifier = Modifier,
)
// Same styling as SessionControlsOverlay but without buttons
```

**
`feature/session/src/main/java/com/koflox/session/presentation/completion/components/RouteMapView.kt`
** (NEW)

```kotlin
@Composable
fun RouteMapView(
    routePoints: List<LatLng>,
    startLocation: LatLng?,
    endLocation: LatLng?,
    modifier: Modifier = Modifier,
)
// GoogleMap with:
// - Blue polyline connecting all route points
// - Optional: start/end markers
// - Camera positioned to show entire route
```

**`feature/session/src/main/java/com/koflox/session/di/PresentationModule.kt`**

- Register `SessionCompletionViewModel` as viewModel with savedStateHandle

### 5. Navigation

**`feature/session/src/main/java/com/koflox/session/navigation/SessionsNavigation.kt`**

- Add `const val SESSION_COMPLETION_ROUTE = "session_completion/{sessionId}"`
- Add `const val SESSION_ID_ARG = "sessionId"`

**`feature/destination-session/bridge/api/.../CyclingSessionUiNavigator.kt`**

- Add method:

```kotlin
@Composable
fun SessionCompletionScreen(
    sessionId: String,
    onBackClick: () -> Unit,
    modifier: Modifier,
)
```

**`feature/destination-session/bridge/impl/.../CyclingSessionUiNavigatorImpl.kt`**

- Implement `SessionCompletionScreen()`

**`app/src/main/java/com/koflox/cyclingassistant/navigation/NavRoutes.kt`**

- Add `const val SESSION_COMPLETION = SESSION_COMPLETION_ROUTE`
- Add helper: `fun sessionCompletion(sessionId: String) = "session_completion/$sessionId"`

**`app/src/main/java/com/koflox/cyclingassistant/navigation/AppNavHost.kt`**

- Add composable route for session completion with argument

### 6. Sessions List Updates

**`feature/session/src/main/java/com/koflox/session/presentation/sessionslist/SessionsListScreen.kt`
**

- Add `onSessionClick: (String) -> Unit` parameter
- Make session cards clickable (only for completed sessions)

**`feature/destination-session/bridge/api/.../CyclingSessionUiNavigator.kt`**

- Update `SessionsScreen` to include `onSessionClick`

**`feature/destination-session/bridge/impl/.../CyclingSessionUiNavigatorImpl.kt`**

- Pass through `onSessionClick`

### 7. String Resources

**`feature/session/src/main/res/values/strings.xml`**

```xml
<!-- Stop Confirmation Dialog -->
<string name="stop_confirmation_title">Stop Session?</string><string
name="stop_confirmation_message">Are you sure you want to stop the current session?
</string><string name="stop_confirmation_cancel">Cancel</string><string
name="stop_confirmation_stop">Stop
</string>

    <!-- Session Completion Screen -->
<string name="session_completion_title">Session Complete</string><string
name="session_completion_back">Back
</string>
```

## Implementation Order

1. Add Google Maps dependency to session module
2. Create `GetSessionByIdUseCase` and register in DI
3. Create `StopConfirmationDialog` and integrate with SessionScreen
4. Make `SessionStatsDisplay` public
5. Create completion screen components (UiState, ViewModel, Screen, RouteMapView,
   SessionSummaryCard)
6. Register completion screen components in DI
7. Update bridge layer with completion screen method
8. Update navigation (routes, AppNavHost)
9. Update sessions list for item click navigation
10. Add string resources
11. Run `./gradlew detektRun` and `./gradlew build`

## UI Mockup

```
┌─────────────────────────────────────┐
│ ← Session Complete                  │  ← TopAppBar
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────────┐         │
│         │                 │         │
│         │   Google Map    │         │
│         │   with blue     │         │
│         │   route line    │         │
│         │                 │         │
│         └─────────────────┘         │
│                                     │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐    │
│  │     Cycling Destination     │    │  ← SessionSummaryCard
│  │                             │    │     (same style as
│  │  Time    Dist   Avg   Top   │    │      SessionControlsOverlay)
│  │ 01:23:45 12.34  18.5  32.1  │    │
│  │   km     km/h   km/h        │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

## Notes

- The map should auto-fit to show the entire route with padding
- Use the same blue color for the route line as used elsewhere in the app
- Session completion screen should handle the case where session is not found (error state)
- Consider adding start/end markers on the map for better visual clarity
