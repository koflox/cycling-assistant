package com.koflox.destinations.presentation.destinations

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koflox.destinations.presentation.destinations.components.GoogleMapView
import com.koflox.destinations.presentation.destinations.components.LetsGoButton
import com.koflox.destinations.presentation.destinations.components.LoadingOverlay
import com.koflox.destinations.presentation.destinations.components.RouteSlider
import com.koflox.destinations.presentation.permission.LocationPermissionHandler
import com.koflox.destinationsession.bridge.CyclingSessionUiNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

@Composable
internal fun DestinationsScreen(
    viewModel: DestinationsViewModel = koinViewModel(),
    sessionUiNavigator: CyclingSessionUiNavigator = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ScreenLifecycleEffects(viewModel)
    ErrorEffect(uiState.error, context, viewModel)
    NavigationEffect(uiState.navigationAction, context, viewModel)
    LocationPermissionHandler(
        onPermissionGranted = { viewModel.onEvent(DestinationsUiEvent.PermissionGranted) },
        onPermissionDenied = { viewModel.onEvent(DestinationsUiEvent.PermissionDenied) },
    ) {
        DestinationsContent(uiState, viewModel, sessionUiNavigator)
    }
}

@Composable
private fun ScreenLifecycleEffects(viewModel: DestinationsViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(DestinationsUiEvent.ScreenResumed)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onEvent(DestinationsUiEvent.ScreenPaused)
    }
}

@Composable
private fun ErrorEffect(error: String?, context: Context, viewModel: DestinationsViewModel) {
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(DestinationsUiEvent.ErrorDismissed)
        }
    }
}

@Composable
private fun NavigationEffect(action: NavigationAction?, context: Context, viewModel: DestinationsViewModel) {
    LaunchedEffect(action) {
        when (action) {
            is NavigationAction.OpenGoogleMaps -> {
                val intent = Intent(Intent.ACTION_VIEW, action.uri).apply { setPackage(GOOGLE_MAPS_PACKAGE) }
                context.startActivity(intent)
                viewModel.onEvent(DestinationsUiEvent.NavigationActionHandled)
            }
            null -> Unit
        }
    }
}

@Composable
private fun DestinationsContent(
    uiState: DestinationsUiState,
    viewModel: DestinationsViewModel,
    sessionUiNavigator: CyclingSessionUiNavigator,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMapView(
            modifier = Modifier.fillMaxSize(),
            selectedDestination = uiState.selectedDestination,
            otherDestinations = if (uiState.isSessionActive) emptyList() else uiState.otherValidDestinations,
            userLocation = uiState.userLocation,
            cameraFocusLocation = uiState.cameraFocusLocation,
            onSelectedMarkerInfoClick = { viewModel.onEvent(DestinationsUiEvent.SelectedMarkerInfoClicked) },
        )

        if (uiState.isSessionActive) {
            uiState.selectedDestination?.let { destination ->
                sessionUiNavigator.SessionScreen(
                    destinationLocation = destination.location,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RouteSlider(
                    distanceKm = uiState.routeDistanceKm,
                    toleranceKm = uiState.toleranceKm,
                    onDistanceChanged = { viewModel.onEvent(DestinationsUiEvent.RouteDistanceChanged(it)) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LetsGoButton(
                    onClick = { viewModel.onEvent(DestinationsUiEvent.LetsGoClicked) },
                    enabled = !uiState.isLoading && uiState.isPermissionGranted,
                )
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }

    if (uiState.showSelectedMarkerOptionsDialog && uiState.selectedDestination != null && uiState.userLocation != null) {
        sessionUiNavigator.DestinationOptions(
            destinationId = uiState.selectedDestination.id,
            destinationName = uiState.selectedDestination.title,
            destinationLocation = uiState.selectedDestination.location,
            distanceKm = uiState.selectedDestination.distanceKm,
            userLocation = uiState.userLocation,
            onNavigateClick = {
                viewModel.onEvent(DestinationsUiEvent.SelectedMarkerOptionsDialogDismissed)
                viewModel.onEvent(DestinationsUiEvent.OpenDestinationInGoogleMaps(uiState.selectedDestination))
            },
            onDismiss = { viewModel.onEvent(DestinationsUiEvent.SelectedMarkerOptionsDialogDismissed) },
        )
    }
}
