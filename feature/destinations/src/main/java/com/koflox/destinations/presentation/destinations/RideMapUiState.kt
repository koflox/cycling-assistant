package com.koflox.destinations.presentation.destinations

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.koflox.designsystem.text.UiText
import com.koflox.destinations.domain.model.DistanceBounds
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.location.model.Location

internal sealed interface RideMapUiState {
    data class Loading(val items: Set<LoadingItem>) : RideMapUiState
    data object LocationDisabled : RideMapUiState
    data class PermissionDenied(val isRationaleAvailable: Boolean) : RideMapUiState
    data class FreeRoamIdle(
        val userLocation: Location?,
        val cameraFocusLocation: Location?,
        val isSessionStarting: Boolean,
        val error: UiText?,
    ) : RideMapUiState

    data class DestinationIdle(
        val userLocation: Location?,
        val cameraFocusLocation: Location?,
        val selectedDestination: DestinationUiModel?,
        val otherValidDestinations: List<DestinationUiModel>,
        val curvePoints: List<LatLng>,
        val routeDistanceKm: Double,
        val toleranceKm: Double,
        val distanceBounds: DistanceBounds?,
        val isPreparingDestinations: Boolean,
        val isCalculatingBounds: Boolean,
        val areDistanceBoundsReady: Boolean,
        val isLoading: Boolean,
        val isSessionStarting: Boolean,
        val showSelectedMarkerOptionsDialog: Boolean,
        val error: UiText?,
        val navigationAction: NavigationAction?,
    ) : RideMapUiState

    data class ActiveSession(
        val userLocation: Location?,
        val cameraFocusLocation: Location?,
        val selectedDestination: DestinationUiModel?,
        val curvePoints: List<LatLng>,
        val showSelectedMarkerOptionsDialog: Boolean,
        val error: UiText?,
        val navigationAction: NavigationAction?,
        val nutritionSuggestionTimeMs: Long?,
    ) : RideMapUiState
}

internal sealed interface NavigationAction {
    data class OpenGoogleMaps(val uri: Uri) : NavigationAction
}

internal val RideMapUiState.error: UiText?
    get() = when (this) {
        is RideMapUiState.FreeRoamIdle -> error
        is RideMapUiState.DestinationIdle -> error
        is RideMapUiState.ActiveSession -> error
        is RideMapUiState.Loading, RideMapUiState.LocationDisabled, is RideMapUiState.PermissionDenied -> null
    }

internal val RideMapUiState.navigationAction: NavigationAction?
    get() = when (this) {
        is RideMapUiState.DestinationIdle -> navigationAction
        is RideMapUiState.ActiveSession -> navigationAction
        else -> null
    }

internal val RideMapUiState.userLocation: Location?
    get() = when (this) {
        is RideMapUiState.FreeRoamIdle -> userLocation
        is RideMapUiState.DestinationIdle -> userLocation
        is RideMapUiState.ActiveSession -> userLocation
        is RideMapUiState.Loading, RideMapUiState.LocationDisabled, is RideMapUiState.PermissionDenied -> null
    }

internal val RideMapUiState.cameraFocusLocation: Location?
    get() = when (this) {
        is RideMapUiState.FreeRoamIdle -> cameraFocusLocation
        is RideMapUiState.DestinationIdle -> cameraFocusLocation
        is RideMapUiState.ActiveSession -> cameraFocusLocation
        is RideMapUiState.Loading, RideMapUiState.LocationDisabled, is RideMapUiState.PermissionDenied -> null
    }

internal val RideMapUiState.selectedDestination: DestinationUiModel?
    get() = when (this) {
        is RideMapUiState.DestinationIdle -> selectedDestination
        is RideMapUiState.ActiveSession -> selectedDestination
        else -> null
    }

internal val RideMapUiState.curvePoints: List<LatLng>
    get() = when (this) {
        is RideMapUiState.DestinationIdle -> curvePoints
        is RideMapUiState.ActiveSession -> curvePoints
        else -> emptyList()
    }
