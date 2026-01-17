package com.koflox.destinations.presentation.destinations

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.NoSuitableDestinationException
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinationsession.bridge.CyclingSessionUseCase
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal class DestinationsViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase,
    private val getDestinationInfoUseCase: GetDestinationInfoUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiMapper: DestinationUiMapper,
    private val application: Application,
    private val cyclingSessionUseCase: CyclingSessionUseCase,
) : AndroidViewModel(application) {

    companion object {
        private const val CAMERA_MOVEMENT_THRESHOLD_METERS = 50.0
        private const val METERS_IN_KILOMETER = 1000.0
        private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }

    private val _uiState = MutableStateFlow(DestinationsUiState())
    val uiState: StateFlow<DestinationsUiState> = _uiState.asStateFlow()

    private var locationObservationJob: Job? = null
    private var isScreenVisible = false

    init {
        initDestinations()
        listenToActiveSession()
        checkSelectedDestination()
    }

    private fun initDestinations() {
        viewModelScope.launch {
            initializeDatabaseUseCase.init()
        }
    }

    private fun checkSelectedDestination() {
        viewModelScope.launch {
            cyclingSessionUseCase.getActiveSessionDestination()?.let { activeDestination ->
                findDestination(destinationId = activeDestination.id)
            }
        }
    }

    private fun listenToActiveSession() {
        viewModelScope.launch {
            cyclingSessionUseCase.observeHasActiveSession().collect { isActive ->
                _uiState.update { it.copy(isSessionActive = isActive) }
            }
        }
    }

    fun onEvent(event: DestinationsUiEvent) {
        when (event) {
            is DestinationsUiEvent.RouteDistanceChanged -> updateRouteDistance(event.distanceKm)
            DestinationsUiEvent.LetsGoClicked -> findDestination()
            DestinationsUiEvent.PermissionGranted -> onPermissionGranted()
            DestinationsUiEvent.PermissionDenied -> onPermissionDenied()
            DestinationsUiEvent.ErrorDismissed -> dismissError()
            DestinationsUiEvent.ScreenResumed -> onScreenResumed()
            DestinationsUiEvent.ScreenPaused -> onScreenPaused()
            is DestinationsUiEvent.OpenDestinationInGoogleMaps -> openInGoogleMaps(event.destination)
            DestinationsUiEvent.NavigationActionHandled -> clearNavigationAction()
            DestinationsUiEvent.SelectedMarkerInfoClicked -> showMarkerOptionsDialog()
            DestinationsUiEvent.SelectedMarkerOptionsDialogDismissed -> dismissSelectedMarkerOptionsDialog()
        }
    }

    private fun findDestination(destinationId: String? = null) {
        viewModelScope.launch {
            val isRecovery = destinationId != null
            val isSessionActive = isRecovery || _uiState.value.isSessionActive
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
                    if (isRecovery) {
                        getSelectedDestination(
                            location = location,
                            destinationId = destinationId,
                            isSessionActive = true,
                            isSessionRecovery = true,
                        )
                    } else {
                        selectRandomDestination(location)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSessionActive = isSessionActive,
                            error = if (isRecovery) null else application.getString(
                                R.string.failed_to_get_location,
                                error.message,
                            ),
                        )
                    }
                }
        }
    }

    private suspend fun selectRandomDestination(location: Location) {
        val currentState = _uiState.value
        handleDestinationsResult(
            result = getDestinationInfoUseCase.getRandomDestinations(
                userLocation = location,
                targetDistanceKm = currentState.routeDistanceKm,
                toleranceKm = currentState.toleranceKm,
            ),
            location = location,
            isSessionActive = false,
            isSessionRecovery = false,
        )
    }

    private suspend fun getSelectedDestination(
        location: Location,
        destinationId: String,
        isSessionActive: Boolean,
        isSessionRecovery: Boolean,
    ) {
        handleDestinationsResult(
            result = getDestinationInfoUseCase.getDestinations(
                userLocation = location,
                toleranceKm = _uiState.value.toleranceKm,
                destinationId = destinationId
            ),
            location = location,
            isSessionActive = isSessionActive,
            isSessionRecovery = isSessionRecovery,
        )
    }

    private suspend fun handleDestinationsResult(
        result: Result<Destinations>,
        location: Location,
        isSessionActive: Boolean,
        isSessionRecovery: Boolean,
    ) {
        result.onSuccess { destinations ->
            val uiModel = uiMapper.toUiModel(destinations, location)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedDestination = uiModel.selected,
                    otherValidDestinations = uiModel.otherValidDestinations,
                    isSessionActive = isSessionActive,
                    routeDistanceKm = if (isSessionRecovery) uiModel.selected.distanceKm.roundToInt().toDouble() else it.routeDistanceKm,
                )
            }
        }.onFailure { error ->
            handleDestinationError(error)
        }
    }

    private fun handleDestinationError(error: Throwable) {
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

    private fun onPermissionDenied() {
        _uiState.update { it.copy(error = application.getString(R.string.error_location_permission_denied)) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun openInGoogleMaps(destination: DestinationUiModel) {
        if (isGoogleMapsInstalled()) {
            val uri = "google.navigation:q=${destination.location.latitude},${destination.location.longitude}&mode=b".toUri()
            _uiState.update { it.copy(navigationAction = NavigationAction.OpenGoogleMaps(uri)) }
        } else {
            _uiState.update { it.copy(error = application.getString(R.string.error_google_maps_not_installed)) }
        }
    }

    private fun isGoogleMapsInstalled(): Boolean {
        return try {
            application.packageManager.getPackageInfo(GOOGLE_MAPS_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun clearNavigationAction() {
        _uiState.update { it.copy(navigationAction = null) }
    }

    private fun showMarkerOptionsDialog() {
        _uiState.update { it.copy(showSelectedMarkerOptionsDialog = true) }
    }

    private fun dismissSelectedMarkerOptionsDialog() {
        _uiState.update { it.copy(showSelectedMarkerOptionsDialog = false) }
    }

}
