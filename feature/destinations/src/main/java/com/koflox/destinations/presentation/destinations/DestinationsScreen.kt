package com.koflox.destinations.presentation.destinations

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun DestinationsScreen(
    viewModel: DestinationsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(DestinationsUiEvent.ScreenResumed)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onEvent(DestinationsUiEvent.ScreenPaused)
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(DestinationsUiEvent.ErrorDismissed)
        }
    }
    LocationPermissionHandler(
        onPermissionGranted = { viewModel.onEvent(DestinationsUiEvent.PermissionGranted) },
        onPermissionDenied = { viewModel.onEvent(DestinationsUiEvent.PermissionDenied) },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMapView(
                modifier = Modifier.fillMaxSize(),
                selectedDestination = uiState.selectedDestination,
                otherDestinations = uiState.otherValidDestinations,
                userLocation = uiState.userLocation,
                cameraFocusLocation = uiState.cameraFocusLocation,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RouteSlider(
                    distanceKm = uiState.routeDistanceKm,
                    onDistanceChanged = {
                        viewModel.onEvent(DestinationsUiEvent.RouteDistanceChanged(it))
                    },
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                LetsGoButton(
                    onClick = { viewModel.onEvent(DestinationsUiEvent.LetsGoClicked) },
                    enabled = !uiState.isLoading && uiState.isPermissionGranted,
                )
            }
            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}
