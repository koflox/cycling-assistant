package com.koflox.destinations.presentation.destinations

import android.net.Uri
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.location.model.Location

internal data class DestinationsUiState(
    val isLoading: Boolean = false,
    val selectedDestination: DestinationUiModel? = null,
    val otherValidDestinations: List<DestinationUiModel> = emptyList(),
    val userLocation: Location? = null,
    val cameraFocusLocation: Location? = null,
    val routeDistanceKm: Double = DEFAULT_ROUTE_DISTANCE_KM,
    val toleranceKm: Double = DEFAULT_TOLERANCE_KM,
    val error: String? = null,
    val isPermissionGranted: Boolean = false,
    val navigationAction: NavigationAction? = null,
) {
    companion object {
        const val DEFAULT_ROUTE_DISTANCE_KM = 15.0
        const val DEFAULT_TOLERANCE_KM = 2.5
    }
}

internal sealed interface NavigationAction {
    data class OpenGoogleMaps(val uri: Uri) : NavigationAction
}
