package com.koflox.destinations.presentation.destinations

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.destinations.R
import com.koflox.destinations.domain.usecase.GetRandomDestinationUseCase
import com.koflox.destinations.domain.usecase.GetRandomDestinationUseCaseImpl.Companion.DEFAULT_TOLERANCE_KM
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.NoSuitableDestinationException
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.domain.util.DistanceCalculator
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.location.model.Location
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DestinationsViewModel(
    private val getRandomDestinationUseCase: GetRandomDestinationUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiMapper: DestinationUiMapper,
    private val application: Application,
) : AndroidViewModel(application) {

    companion object {
        private const val CAMERA_MOVEMENT_THRESHOLD_METERS = 50.0
        private const val METERS_IN_KILOMETER = 1000.0
    }

    private val _uiState = MutableStateFlow(DestinationsUiState())
    val uiState: StateFlow<DestinationsUiState> = _uiState.asStateFlow()

    private var locationObservationJob: Job? = null
    private var isScreenVisible = false

    init {
        viewModelScope.launch {
            initializeDatabaseUseCase.init()
        }
    }

    fun onEvent(event: DestinationsUiEvent) {
        when (event) {
            is DestinationsUiEvent.RouteDistanceChanged -> updateRouteDistance(event.distanceKm)
            DestinationsUiEvent.LetsGoClicked -> findRandomDestination()
            DestinationsUiEvent.PermissionGranted -> onPermissionGranted()
            DestinationsUiEvent.PermissionDenied -> onPermissionDenied()
            DestinationsUiEvent.ErrorDismissed -> dismissError()
            DestinationsUiEvent.ScreenResumed -> onScreenResumed()
            DestinationsUiEvent.ScreenPaused -> onScreenPaused()
        }
    }

    private fun findRandomDestination() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedDestination = null,
                    otherValidDestinations = emptyList(),
                )
            }

            getUserLocationUseCase.getLocation()
                .onSuccess { location ->
                    _uiState.update { it.copy(userLocation = location) }
                    selectDestination(location)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = application.getString(R.string.failed_to_get_location, error.message),
                        )
                    }
                }
        }
    }

    private suspend fun selectDestination(location: Location) {
        val targetDistance = _uiState.value.routeDistanceKm

        getRandomDestinationUseCase.getDestinations(
            userLocation = location,
            targetDistanceKm = targetDistance,
            toleranceKm = DEFAULT_TOLERANCE_KM,
            areAllValidDestinationsIncluded = true,
        ).onSuccess { destinations ->
            val uiModel = uiMapper.toUiModel(destinations, location)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedDestination = uiModel.selected,
                    otherValidDestinations = uiModel.otherValidDestinations,
                )
            }
        }.onFailure { error ->
            val message = when (error) {
                is NoSuitableDestinationException -> application.getString(R.string.error_too_far_from_supported_area)
                else -> application.getString(R.string.error_not_handled)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = message,
                )
            }
        }
    }

    private fun updateRouteDistance(distanceKm: Double) {
        _uiState.update { it.copy(routeDistanceKm = distanceKm) }
    }

    private fun onPermissionGranted() {
        _uiState.update { it.copy(isPermissionGranted = true) }
        if (isScreenVisible) {
            startLocationObservation()
        }
    }

    private fun onScreenResumed() {
        isScreenVisible = true
        if (_uiState.value.isPermissionGranted) {
            startLocationObservation()
        }
    }

    private fun onScreenPaused() {
        isScreenVisible = false
        stopLocationObservation()
    }

    private fun stopLocationObservation() {
        locationObservationJob?.cancel()
        locationObservationJob = null
    }

    private fun startLocationObservation() {
        locationObservationJob?.cancel()
        locationObservationJob = viewModelScope.launch {
            observeUserLocationUseCase.observe().collect { newLocation ->
                val currentState = _uiState.value
                val shouldMoveCameraToUserLocation = shouldUpdateCameraFocus(
                    currentFocus = currentState.cameraFocusLocation,
                    newLocation = newLocation,
                )
                _uiState.update {
                    it.copy(
                        userLocation = newLocation,
                        cameraFocusLocation = if (shouldMoveCameraToUserLocation) {
                            newLocation
                        } else {
                            it.cameraFocusLocation
                        },
                    )
                }
            }
        }
    }

    private suspend fun shouldUpdateCameraFocus(currentFocus: Location?, newLocation: Location): Boolean {
        if (currentFocus == null) return true
        val distanceMeters = distanceCalculator.calculate(
            lat1 = currentFocus.latitude,
            lon1 = currentFocus.longitude,
            lat2 = newLocation.latitude,
            lon2 = newLocation.longitude,
        ) * METERS_IN_KILOMETER
        return distanceMeters >= CAMERA_MOVEMENT_THRESHOLD_METERS
    }

    private fun onPermissionDenied() {
        _uiState.update { it.copy(error = application.getString(R.string.error_location_permission_denied)) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

}
