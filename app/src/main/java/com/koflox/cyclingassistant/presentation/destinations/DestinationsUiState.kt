package com.koflox.cyclingassistant.presentation.destinations

import com.koflox.cyclingassistant.domain.model.Location
import com.koflox.cyclingassistant.presentation.destinations.model.DestinationUiModel

internal data class DestinationsUiState(
    val isLoading: Boolean = false,
    val selectedDestination: DestinationUiModel? = null,
    val otherValidDestinations: List<DestinationUiModel> = emptyList(),
    val userLocation: Location? = null,
    val routeDistanceKm: Double = DEFAULT_ROUTE_DISTANCE_KM,
    val error: String? = null,
    val isPermissionGranted: Boolean = false,
) {
    companion object {
        const val DEFAULT_ROUTE_DISTANCE_KM = 15.0
    }
}
