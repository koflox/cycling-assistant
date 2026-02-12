package com.koflox.destinations.presentation.destinations.delegate

import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.presentation.destinations.RideMapInternalState
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LocationDelegate(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiState: MutableStateFlow<RideMapInternalState>,
    private val scope: CoroutineScope,
    private val onLocationUpdated: (Location) -> Unit,
) {

    companion object {
        private const val CAMERA_MOVEMENT_THRESHOLD_METERS = 50.0
        private const val METERS_IN_KILOMETER = 1000.0
    }

    private var locationObservationJob: Job? = null

    suspend fun fetchInitialLocation(): Location? {
        var result: Location? = null
        getUserLocationUseCase.getLocation()
            .onSuccess { location ->
                result = location
                uiState.update {
                    it.copy(
                        userLocation = location,
                        cameraFocusLocation = it.cameraFocusLocation ?: location,
                    )
                }
            }
        return result
    }

    fun startLocationObservation() {
        locationObservationJob?.cancel()
        locationObservationJob = scope.launch {
            observeUserLocationUseCase.observe().collect { newLocation ->
                val currentState = uiState.value
                val shouldMoveCamera = shouldUpdateCameraFocus(
                    currentFocus = currentState.cameraFocusLocation,
                    newLocation = newLocation,
                )
                uiState.update {
                    it.copy(
                        userLocation = newLocation,
                        cameraFocusLocation = if (shouldMoveCamera) newLocation else it.cameraFocusLocation,
                    )
                }
                onLocationUpdated(newLocation)
            }
        }
    }

    fun stopLocationObservation() {
        locationObservationJob?.cancel()
        locationObservationJob = null
    }

    private fun shouldUpdateCameraFocus(currentFocus: Location?, newLocation: Location): Boolean {
        if (currentFocus == null) return true
        val distanceMeters = distanceCalculator.calculateKm(
            lat1 = currentFocus.latitude,
            lon1 = currentFocus.longitude,
            lat2 = newLocation.latitude,
            lon2 = newLocation.longitude,
        ) * METERS_IN_KILOMETER
        return distanceMeters >= CAMERA_MOVEMENT_THRESHOLD_METERS
    }
}
