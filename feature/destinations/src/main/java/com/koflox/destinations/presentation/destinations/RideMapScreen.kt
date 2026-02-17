package com.koflox.destinations.presentation.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koflox.designsystem.component.ActionCard
import com.koflox.designsystem.component.StatusCard
import com.koflox.designsystem.text.UiText
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing
import com.koflox.destinationnutrition.bridge.navigator.NutritionUiNavigator
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.presentation.destinations.components.GoogleMapView
import com.koflox.destinations.presentation.destinations.components.LetsGoButton
import com.koflox.destinations.presentation.destinations.components.LoadingOverlay
import com.koflox.destinations.presentation.destinations.components.LocationRetryCard
import com.koflox.destinations.presentation.destinations.components.RidingModeToggle
import com.koflox.destinations.presentation.destinations.components.RouteSlider
import com.koflox.destinations.presentation.destinations.components.StartRideButton
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.permission.LocationPermissionHandler
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.location.settings.LocationSettingsHandler
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

@Composable
fun RideMapScreen(
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    RideMapRoute(
        onNavigateToSessionCompletion = onNavigateToSessionCompletion,
        modifier = modifier,
    )
}

@Composable
internal fun RideMapRoute(
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RideMapViewModel = koinViewModel(),
    sessionUiNavigator: CyclingSessionUiNavigator = koinInject(),
    nutritionUiNavigator: NutritionUiNavigator = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var retryTrigger by remember { mutableIntStateOf(0) }
    ScreenLifecycleEffects(
        onScreenResumed = { viewModel.onEvent(RideMapUiEvent.LifecycleEvent.ScreenResumed) },
        onScreenPaused = { viewModel.onEvent(RideMapUiEvent.LifecycleEvent.ScreenPaused) },
    )
    ErrorEffect(
        error = uiState.error,
        context = context,
        onErrorDismissed = { viewModel.onEvent(RideMapUiEvent.CommonEvent.ErrorDismissed) },
    )
    NavigationEffect(
        action = uiState.navigationAction,
        context = context,
        onNavigationActionHandled = { viewModel.onEvent(RideMapUiEvent.CommonEvent.NavigationActionHandled) },
    )
    LocationPermissionHandler(
        onPermissionGranted = { viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted) },
        onPermissionDenied = { isRationaleAvailable ->
            viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionDenied(isRationaleAvailable))
        },
        retryTrigger = retryTrigger,
    ) {
        RideMapContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            sessionUiNavigator = sessionUiNavigator,
            nutritionUiNavigator = nutritionUiNavigator,
            modifier = modifier,
            onNavigateToSessionCompletion = onNavigateToSessionCompletion,
            onRetryPermission = { retryTrigger++ },
        )
    }
}

@Composable
private fun ScreenLifecycleEffects(
    onScreenResumed: () -> Unit,
    onScreenPaused: () -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onScreenResumed()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        onScreenPaused()
    }
}

@Composable
private fun ErrorEffect(
    error: UiText?,
    context: Context,
    onErrorDismissed: () -> Unit,
) {
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it.resolve(context), Toast.LENGTH_SHORT).show()
            onErrorDismissed()
        }
    }
}

@Composable
private fun NavigationEffect(
    action: NavigationAction?,
    context: Context,
    onNavigationActionHandled: () -> Unit,
) {
    LaunchedEffect(action) {
        when (action) {
            is NavigationAction.OpenGoogleMaps -> {
                val intent = Intent(Intent.ACTION_VIEW, action.uri).apply { setPackage(GOOGLE_MAPS_PACKAGE) }
                context.startActivity(intent)
                onNavigationActionHandled()
            }
            null -> Unit
        }
    }
}

@Composable
private fun RideMapContent(
    uiState: RideMapUiState,
    onEvent: (RideMapUiEvent) -> Unit,
    sessionUiNavigator: CyclingSessionUiNavigator,
    nutritionUiNavigator: NutritionUiNavigator,
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    onRetryPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMapView(
            modifier = Modifier.fillMaxSize(),
            selectedDestination = uiState.selectedDestination,
            otherDestinations = (uiState as? RideMapUiState.DestinationIdle)?.otherValidDestinations ?: emptyList(),
            userLocation = uiState.userLocation,
            cameraFocusLocation = uiState.cameraFocusLocation,
            curvePoints = uiState.curvePoints,
            isSessionActive = uiState is RideMapUiState.ActiveSession,
            onSelectedMarkerInfoClick = { onEvent(RideMapUiEvent.DestinationEvent.SelectedMarkerInfoClicked) },
            onMapLoaded = { onEvent(RideMapUiEvent.MapEvent.MapLoaded) },
        )
        RideMapOverlay(uiState, onEvent, sessionUiNavigator, nutritionUiNavigator, onNavigateToSessionCompletion, onRetryPermission)
    }
    when (uiState) {
        is RideMapUiState.FreeRoamIdle -> FreeRoamSessionGate(
            uiState = uiState,
            sessionUiNavigator = sessionUiNavigator,
        )
        is RideMapUiState.DestinationIdle -> DestinationOptionsDialog(
            showDialog = uiState.showSelectedMarkerOptionsDialog,
            selectedDestination = uiState.selectedDestination,
            onSessionStarting = { onEvent(RideMapUiEvent.SessionEvent.DestinationSessionStarting) },
            onNavigateClick = { destination ->
                onEvent(RideMapUiEvent.DestinationEvent.SelectedMarkerOptionsDialogDismissed)
                onEvent(RideMapUiEvent.DestinationEvent.OpenInGoogleMaps(destination))
            },
            onDialogDismiss = { onEvent(RideMapUiEvent.DestinationEvent.SelectedMarkerOptionsDialogDismissed) },
            sessionUiNavigator = sessionUiNavigator,
        )
        is RideMapUiState.ActiveSession -> DestinationOptionsDialog(
            showDialog = uiState.showSelectedMarkerOptionsDialog,
            selectedDestination = uiState.selectedDestination,
            onSessionStarting = { onEvent(RideMapUiEvent.SessionEvent.DestinationSessionStarting) },
            onNavigateClick = { destination ->
                onEvent(RideMapUiEvent.DestinationEvent.SelectedMarkerOptionsDialogDismissed)
                onEvent(RideMapUiEvent.DestinationEvent.OpenInGoogleMaps(destination))
            },
            onDialogDismiss = { onEvent(RideMapUiEvent.DestinationEvent.SelectedMarkerOptionsDialogDismissed) },
            sessionUiNavigator = sessionUiNavigator,
        )
        else -> Unit
    }
}

@Composable
private fun BoxScope.RideMapOverlay(
    uiState: RideMapUiState,
    onEvent: (RideMapUiEvent) -> Unit,
    sessionUiNavigator: CyclingSessionUiNavigator,
    nutritionUiNavigator: NutritionUiNavigator,
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    onRetryPermission: () -> Unit,
) {
    when (uiState) {
        is RideMapUiState.Loading -> {
            LoadingStatusCard(
                items = uiState.items,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Spacing.Large),
            )
        }
        RideMapUiState.LocationDisabled -> LocationRetryOverlay(
            onRetryInitialization = { onEvent(RideMapUiEvent.LifecycleEvent.RetryInitializationClicked) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.Large),
        )
        is RideMapUiState.PermissionDenied -> PermissionDeniedOverlay(
            isRationaleAvailable = uiState.isRationaleAvailable,
            onRetryPermission = onRetryPermission,
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.Large),
        )
        is RideMapUiState.FreeRoamIdle -> IdleOverlay(
            uiState = uiState,
            onModeSelected = { onEvent(RideMapUiEvent.ModeEvent.ModeSelected(it)) },
            onFreeRoamSessionStarting = { onEvent(RideMapUiEvent.SessionEvent.FreeRoamSessionStarting) },
        )
        is RideMapUiState.DestinationIdle -> IdleOverlay(
            uiState = uiState,
            onModeSelected = { onEvent(RideMapUiEvent.ModeEvent.ModeSelected(it)) },
            onRouteDistanceChanged = { onEvent(RideMapUiEvent.DestinationEvent.RouteDistanceChanged(it)) },
            onLetsGoClicked = { onEvent(RideMapUiEvent.DestinationEvent.LetsGoClicked) },
        )
        is RideMapUiState.ActiveSession -> ActiveSessionControls(
            uiState = uiState,
            onNutritionPopupDismiss = { onEvent(RideMapUiEvent.CommonEvent.NutritionPopupDismissed) },
            sessionUiNavigator = sessionUiNavigator,
            nutritionUiNavigator = nutritionUiNavigator,
            onNavigateToSessionCompletion = onNavigateToSessionCompletion,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun BoxScope.IdleOverlay(
    uiState: RideMapUiState.FreeRoamIdle,
    onModeSelected: (RidingMode) -> Unit,
    onFreeRoamSessionStarting: () -> Unit,
) {
    var isModeExpanded by remember { mutableStateOf(false) }
    ModeToggleDismissOverlay(isModeExpanded) { isModeExpanded = false }
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(Spacing.Large),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            RidingModeToggle(
                selectedMode = RidingMode.FREE_ROAM,
                isExpanded = isModeExpanded,
                onToggleExpand = { isModeExpanded = !isModeExpanded },
                onModeSelected = {
                    isModeExpanded = false
                    onModeSelected(it)
                },
                modifier = Modifier.padding(end = Spacing.Large),
            )
        }
        FreeRoamControls(uiState = uiState, onFreeRoamSessionStarting = onFreeRoamSessionStarting)
        Spacer(modifier = Modifier.weight(1f))
    }
    if (uiState.isSessionStarting) {
        LoadingOverlay()
    }
}

@Composable
private fun BoxScope.IdleOverlay(
    uiState: RideMapUiState.DestinationIdle,
    onModeSelected: (RidingMode) -> Unit,
    onRouteDistanceChanged: (Double) -> Unit,
    onLetsGoClicked: () -> Unit,
) {
    var isModeExpanded by remember { mutableStateOf(false) }
    ModeToggleDismissOverlay(isModeExpanded) { isModeExpanded = false }
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DestinationStatusContent(uiState, onRouteDistanceChanged)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                RidingModeToggle(
                    selectedMode = RidingMode.DESTINATION,
                    isExpanded = isModeExpanded,
                    onToggleExpand = { isModeExpanded = !isModeExpanded },
                    onModeSelected = {
                        isModeExpanded = false
                        onModeSelected(it)
                    },
                    modifier = Modifier.padding(end = Spacing.Large),
                )
            }
            DestinationActionButton(uiState, onLetsGoClicked)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    if (uiState.isLoading || uiState.isSessionStarting) {
        LoadingOverlay()
    }
}

@Composable
private fun FreeRoamSessionGate(
    uiState: RideMapUiState.FreeRoamIdle,
    sessionUiNavigator: CyclingSessionUiNavigator,
) {
    if (uiState.isSessionStarting) {
        sessionUiNavigator.StartFreeRoamSession()
    }
}

@Composable
private fun DestinationOptionsDialog(
    showDialog: Boolean,
    selectedDestination: DestinationUiModel?,
    onSessionStarting: () -> Unit,
    onNavigateClick: (DestinationUiModel) -> Unit,
    onDialogDismiss: () -> Unit,
    sessionUiNavigator: CyclingSessionUiNavigator,
) {
    if (showDialog && selectedDestination != null) {
        sessionUiNavigator.DestinationOptions(
            destinationId = selectedDestination.id,
            destinationName = selectedDestination.title,
            destinationLocation = selectedDestination.location,
            distanceKm = selectedDestination.distanceKm,
            onSessionStarting = onSessionStarting,
            onNavigateClick = { onNavigateClick(selectedDestination) },
            onDismiss = onDialogDismiss,
        )
    }
}

@Composable
private fun ActiveSessionControls(
    uiState: RideMapUiState.ActiveSession,
    onNutritionPopupDismiss: () -> Unit,
    sessionUiNavigator: CyclingSessionUiNavigator,
    nutritionUiNavigator: NutritionUiNavigator,
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        uiState.nutritionSuggestionTimeMs?.let { suggestionTimeMs ->
            nutritionUiNavigator.NutritionBreakPopup(
                suggestionTimeMs = suggestionTimeMs,
                onDismiss = onNutritionPopupDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large),
            )
        }
        sessionUiNavigator.SessionScreen(
            destinationLocation = uiState.selectedDestination?.location,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.Tiny)
                .padding(bottom = Spacing.Large)
                .padding(horizontal = Spacing.Large),
            onNavigateToCompletion = onNavigateToSessionCompletion,
        )
    }
}

@Composable
private fun FreeRoamControls(
    uiState: RideMapUiState.FreeRoamIdle,
    onFreeRoamSessionStarting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldCheckLocation by remember { mutableStateOf(false) }
    if (shouldCheckLocation) {
        LocationSettingsHandler(
            onLocationEnabled = {
                shouldCheckLocation = false
                onFreeRoamSessionStarting()
            },
            onLocationDenied = {
                @Suppress("AssignedValueIsNeverRead")
                shouldCheckLocation = false
            },
        )
    }
    StartRideButton(
        onClick = { shouldCheckLocation = true },
        enabled = !uiState.isSessionStarting,
        modifier = modifier,
    )
}

@Composable
private fun DestinationStatusContent(
    uiState: RideMapUiState.DestinationIdle,
    onRouteDistanceChanged: (Double) -> Unit,
) {
    when {
        uiState.isPreparingDestinations -> StatusCard(
            message = stringResource(R.string.preparing_destinations),
            isLoading = true,
            modifier = Modifier.padding(bottom = Spacing.Large),
        )
        uiState.isCalculatingBounds -> StatusCard(
            message = stringResource(R.string.destination_calculating_bounds),
            isLoading = true,
            modifier = Modifier.padding(bottom = Spacing.Large),
        )
        uiState.distanceBounds != null -> RouteSlider(
            distanceKm = uiState.routeDistanceKm,
            toleranceKm = uiState.toleranceKm,
            minDistanceKm = uiState.distanceBounds.minKm,
            maxDistanceKm = uiState.distanceBounds.maxKm,
            onDistanceChanged = onRouteDistanceChanged,
            modifier = Modifier.padding(bottom = Spacing.Large),
        )
        else -> StatusCard(
            message = stringResource(R.string.destination_no_destinations_in_area),
            isLoading = false,
            modifier = Modifier.padding(bottom = Spacing.Large),
        )
    }
}

@Composable
private fun DestinationActionButton(
    uiState: RideMapUiState.DestinationIdle,
    onLetsGoClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldCheckLocation by remember { mutableStateOf(false) }
    if (shouldCheckLocation) {
        LocationSettingsHandler(
            onLocationEnabled = {
                shouldCheckLocation = false
                onLetsGoClicked()
            },
            onLocationDenied = {
                @Suppress("AssignedValueIsNeverRead")
                shouldCheckLocation = false
            },
        )
    }
    LetsGoButton(
        onClick = { shouldCheckLocation = true },
        enabled = !uiState.isLoading && uiState.areDistanceBoundsReady,
        modifier = modifier,
    )
}

@Composable
private fun ModeToggleDismissOverlay(isVisible: Boolean, onDismiss: () -> Unit) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        )
    }
}

@Composable
private fun LoadingStatusCard(items: Set<LoadingItem>, modifier: Modifier = Modifier) {
    val mapLabel = stringResource(R.string.loading_item_map)
    val locationLabel = stringResource(R.string.loading_item_location)
    val label = items.joinToString { item ->
        when (item) {
            LoadingItem.Map -> mapLabel
            LoadingItem.UserLocation -> locationLabel
        }
    }
    StatusCard(
        message = stringResource(R.string.loading_prefix, label),
        isLoading = true,
        modifier = modifier,
    )
}

@Composable
private fun LocationRetryOverlay(
    onRetryInitialization: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldCheckLocation by remember { mutableStateOf(false) }
    if (shouldCheckLocation) {
        LocationSettingsHandler(
            onLocationEnabled = {
                shouldCheckLocation = false
                onRetryInitialization()
            },
            onLocationDenied = {
                @Suppress("AssignedValueIsNeverRead")
                shouldCheckLocation = false
            },
        )
    }
    LocationRetryCard(
        onEnableLocationClick = { shouldCheckLocation = true },
        modifier = modifier,
    )
}

@Composable
private fun PermissionDeniedOverlay(
    isRationaleAvailable: Boolean,
    onRetryPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val message = if (isRationaleAvailable) {
        stringResource(R.string.permission_location_required)
    } else {
        stringResource(R.string.permission_location_open_settings)
    }
    val buttonLabel = if (isRationaleAvailable) {
        stringResource(R.string.permission_button_grant)
    } else {
        stringResource(R.string.permission_button_open_settings)
    }
    ActionCard(
        message = message,
        buttonLabel = buttonLabel,
        onButtonClick = {
            if (isRationaleAvailable) {
                onRetryPermission()
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        },
        modifier = modifier,
    )
}
