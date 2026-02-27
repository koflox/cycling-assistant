package com.koflox.destinations.presentation.destinations.delegate

import com.koflox.destinations.presentation.destinations.RideMapInternalState
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.location.usecase.GetUserLocationUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LocationDelegate(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiState: MutableStateFlow<RideMapInternalState>,
    private val scope: CoroutineScope,
) {

    companion object {
        private const val IDLE_LOCATION_INTERVAL_MS = 15_000L
        private const val IDLE_MIN_UPDATE_DISTANCE_METERS = 50F
        private const val PAUSE_LOCATION_INTERVAL_MS = 5_000L
        private const val PAUSE_MIN_UPDATE_DISTANCE_METERS = 10F
        private const val CAMERA_MOVEMENT_THRESHOLD_METERS = 50.0
        private const val METERS_IN_KILOMETER = 1000.0
    }

    private val _observedLocations = MutableSharedFlow<Location>()
    val observedLocations: SharedFlow<Location> = _observedLocations.asSharedFlow()

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

    fun updateUserLocation(newLocation: Location) {
        val shouldMoveCamera = shouldUpdateCameraFocus(uiState.value.cameraFocusLocation, newLocation)
        uiState.update {
            it.copy(
                userLocation = newLocation,
                cameraFocusLocation = if (shouldMoveCamera) newLocation else it.cameraFocusLocation,
            )
        }
    }

    fun startLocationObservation() {
        locationObservationJob?.cancel()
        locationObservationJob = scope.launch {
            observeUserLocationUseCase.observe(IDLE_LOCATION_INTERVAL_MS, IDLE_MIN_UPDATE_DISTANCE_METERS)
                .collect { newLocation ->
                    updateUserLocation(newLocation)
                    _observedLocations.emit(newLocation)
                }
        }
    }

    fun startPauseLocationObservation() {
        locationObservationJob?.cancel()
        locationObservationJob = scope.launch {
            observeUserLocationUseCase.observe(PAUSE_LOCATION_INTERVAL_MS, PAUSE_MIN_UPDATE_DISTANCE_METERS)
                .collect { newLocation ->
                    updateUserLocation(newLocation)
                    _observedLocations.emit(newLocation)
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
