package com.koflox.destinations.presentation.destinations

import com.google.android.gms.maps.model.LatLng
import com.koflox.destinations.domain.model.DistanceBounds
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.location.model.Location

internal data class RideMapInternalState(
    val ridingMode: RidingMode = RidingMode.FREE_ROAM,
    val isInitializing: Boolean = true,
    val isPreparingDestinations: Boolean = false,
    val areDestinationsReady: Boolean = false,
    val isLoading: Boolean = false,
    val selectedDestination: DestinationUiModel? = null,
    val otherValidDestinations: List<DestinationUiModel> = emptyList(),
    val userLocation: Location? = null,
    val cameraFocusLocation: Location? = null,
    val curvePoints: List<LatLng> = emptyList(),
    val routeDistanceKm: Double = 0.0,
    val toleranceKm: Double = 0.0,
    val distanceBounds: DistanceBounds? = null,
    val isCalculatingBounds: Boolean = false,
    val error: String? = null,
    val isPermissionGranted: Boolean = false,
    val isPermissionDenied: Boolean = false,
    val isRationaleAvailable: Boolean = false,
    val navigationAction: NavigationAction? = null,
    val showSelectedMarkerOptionsDialog: Boolean = false,
    val isSessionActive: Boolean = false,
    val isActiveSessionChecked: Boolean = false,
    val nutritionSuggestionTimeMs: Long? = null,
    val isLocationDisabled: Boolean = false,
    val isSessionStarting: Boolean = false,
    val isMapLoaded: Boolean = false,
) {
    val isFreeRoam: Boolean
        get() = ridingMode == RidingMode.FREE_ROAM

    val areDistanceBoundsReady: Boolean
        get() = distanceBounds != null && !isCalculatingBounds

    val isReady: Boolean
        get() = !isInitializing && isActiveSessionChecked && isPermissionGranted && userLocation != null

    val isLocationRetryNeeded: Boolean
        get() = isLocationDisabled && !isSessionActive && userLocation == null
}
