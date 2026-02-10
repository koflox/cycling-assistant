package com.koflox.destinations.presentation.destinations

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.koflox.destinationnutrition.bridge.model.NutritionBreakEvent
import com.koflox.destinationnutrition.bridge.usecase.ObserveNutritionBreakUseCase
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.usecase.CheckLocationEnabledUseCase
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.NoSuitableDestinationException
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.domain.usecase.ToleranceCalculator
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.distance.DistanceCalculator
import com.koflox.graphics.curves.createCurvePoints
import com.koflox.graphics.primitives.Point
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

internal class DestinationsViewModel(
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase,
    private val getDestinationInfoUseCase: GetDestinationInfoUseCase,
    private val getDistanceBoundsUseCase: GetDistanceBoundsUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiMapper: DestinationUiMapper,
    private val application: Application,
    private val cyclingSessionUseCase: CyclingSessionUseCase,
    private val observeNutritionBreakUseCase: ObserveNutritionBreakUseCase,
    private val toleranceCalculator: ToleranceCalculator,
    private val dispatcherDefault: CoroutineDispatcher,
) : AndroidViewModel(application) {

    companion object {
        private const val CAMERA_MOVEMENT_THRESHOLD_METERS = 50.0
        private const val DESTINATION_RELOAD_THRESHOLD_KM = 50.0 // TODO: should be unified with DestinationFileResolverImpl
        private const val BOUNDS_RECALCULATION_THRESHOLD_KM = 1.0
        private const val METERS_IN_KILOMETER = 1000.0
        private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }

    private val _uiState = MutableStateFlow(DestinationsUiState())
    val uiState: StateFlow<DestinationsUiState> = _uiState.asStateFlow()

    private var locationObservationJob: Job? = null
    private var isScreenVisible = false
    private val lastDestinationLoadLocation = AtomicReference<Location?>(null)
    private val lastBoundsCalculationLocation = AtomicReference<Location?>(null)

    init {
        initialize()
    }

    private fun initialize() {
        observeLocationEnabled()
        initializeInternal()
    }

    private fun initializeInternal() {
        viewModelScope.launch(dispatcherDefault) {
            _uiState.update { it.copy(isInitializing = true) }
            getUserLocationUseCase.getLocation()
                .onSuccess { location ->
                    _uiState.update {
                        it.copy(
                            userLocation = location,
                            cameraFocusLocation = location,
                        )
                    }
                    startDestinationLoading(location)
                }
            checkActiveSession()
            _uiState.update { it.copy(isInitializing = false) }
            listenToActiveSession()
            observeNutritionEvents()
        }
    }

    private fun observeLocationEnabled() {
        viewModelScope.launch(dispatcherDefault) {
            checkLocationEnabledUseCase.observeLocationEnabled().collect { isEnabled ->
                _uiState.update { it.copy(isLocationDisabled = !isEnabled) }
            }
        }
    }

    private fun startDestinationLoading(location: Location) {
        viewModelScope.launch(dispatcherDefault) {
            lastDestinationLoadLocation.set(location)
            initializeDatabaseUseCase.init(location).collect { event ->
                when (event) {
                    is DestinationLoadingEvent.Loading -> {
                        _uiState.update {
                            it.copy(
                                isPreparingDestinations = true,
                                areDestinationsReady = false,
                            )
                        }
                    }

                    is DestinationLoadingEvent.Completed -> {
                        _uiState.update {
                            it.copy(
                                isPreparingDestinations = false,
                                areDestinationsReady = true,
                            )
                        }
                        calculateDistanceBounds(location)
                    }

                    is DestinationLoadingEvent.Error -> {
                        _uiState.update {
                            it.copy(
                                isPreparingDestinations = false,
                                error = application.getString(R.string.error_not_handled),
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun calculateDistanceBounds(location: Location) {
        _uiState.update { it.copy(isCalculatingBounds = true) }
        getDistanceBoundsUseCase.getBounds(location)
            .onSuccess { bounds ->
                _uiState.update { state ->
                    if (bounds != null) {
                        val routeDistance = if (state.routeDistanceKm == 0.0) {
                            (bounds.minKm + bounds.maxKm) / 4
                        } else {
                            state.routeDistanceKm.coerceIn(bounds.minKm, bounds.maxKm)
                        }
                        state.copy(
                            distanceBounds = bounds,
                            routeDistanceKm = routeDistance,
                            toleranceKm = toleranceCalculator.calculateKm(routeDistance),
                            isCalculatingBounds = false,
                        )
                    } else {
                        state.copy(
                            distanceBounds = null,
                            isCalculatingBounds = false,
                        )
                    }
                }
                lastBoundsCalculationLocation.set(location)
            }
            .onFailure {
                _uiState.update { it.copy(isCalculatingBounds = false) }
            }
    }

    private suspend fun checkActiveSession() {
        val activeDestination = cyclingSessionUseCase.getActiveSessionDestination()
        _uiState.update {
            it.copy(
                isActiveSessionChecked = true,
                isSessionActive = activeDestination != null,
            )
        }
        activeDestination?.let { findDestination(destinationId = it.id) }
    }

    private fun listenToActiveSession() {
        viewModelScope.launch(dispatcherDefault) {
            cyclingSessionUseCase.observeHasActiveSession().collect { isActive ->
                _uiState.update { it.copy(isSessionActive = isActive) }
            }
        }
    }

    fun onEvent(event: DestinationsUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
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
                DestinationsUiEvent.NutritionPopupDismissed -> dismissNutritionPopup()
                DestinationsUiEvent.RetryInitializationClicked -> initializeInternal()
//                DestinationsUiEvent.RetryInitializationClicked -> retryInitialization()
            }
        }
    }

    private suspend fun findDestination(destinationId: String? = null) {
        val isRecovery = destinationId != null
        val isSessionActive = isRecovery || _uiState.value.isSessionActive
        _uiState.update {
            it.copy(
                isLoading = true,
                selectedDestination = null,
                otherValidDestinations = emptyList(),
                curvePoints = emptyList(),
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

    private suspend fun selectRandomDestination(location: Location) {
        handleDestinationsResult(
            result = getDestinationInfoUseCase.getRandomDestinations(
                userLocation = location,
                targetDistanceKm = _uiState.value.routeDistanceKm,
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
                destinationId = destinationId,
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
            val curvePoints = computeCurvePoints(location, uiModel.selected)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedDestination = uiModel.selected,
                    otherValidDestinations = uiModel.otherValidDestinations,
                    curvePoints = curvePoints,
                    isSessionActive = isSessionActive,
                    routeDistanceKm = if (isSessionRecovery) uiModel.selected.distanceKm.roundToInt().toDouble() else it.routeDistanceKm,
                    toleranceKm = if (isSessionRecovery) {
                        toleranceCalculator.calculateKm(uiModel.selected.distanceKm.roundToInt().toDouble())
                    } else {
                        it.toleranceKm
                    },
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
        _uiState.update {
            it.copy(
                routeDistanceKm = distanceKm,
                toleranceKm = toleranceCalculator.calculateKm(distanceKm),
            )
        }
    }

    private fun onPermissionGranted() {
        _uiState.update { it.copy(isPermissionGranted = true) }
        fetchInitialLocationAndStartLoading()
        if (isScreenVisible) {
            startLocationObservation()
        }
    }

    private fun fetchInitialLocationAndStartLoading() {
        viewModelScope.launch(dispatcherDefault) {
            getUserLocationUseCase.getLocation().onSuccess { location ->
                _uiState.update {
                    it.copy(
                        userLocation = location,
                        cameraFocusLocation = it.cameraFocusLocation ?: location,
                    )
                }
                checkAndReloadDestinationsIfNeeded(location)
            }
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
        locationObservationJob = viewModelScope.launch(dispatcherDefault) {
            observeUserLocationUseCase.observe().collect { newLocation ->
                val currentState = _uiState.value
                val shouldMoveCameraToUserLocation = shouldUpdateCameraFocus(
                    currentFocus = currentState.cameraFocusLocation,
                    newLocation = newLocation,
                )
                val updatedCurvePoints = currentState.selectedDestination?.let {
                    computeCurvePoints(newLocation, it)
                } ?: currentState.curvePoints
                _uiState.update {
                    it.copy(
                        userLocation = newLocation,
                        cameraFocusLocation = if (shouldMoveCameraToUserLocation) {
                            newLocation
                        } else {
                            it.cameraFocusLocation
                        },
                        curvePoints = updatedCurvePoints,
                    )
                }
                checkAndReloadDestinationsIfNeeded(newLocation)
                checkAndRecalculateBoundsIfNeeded(newLocation)
            }
        }
    }

    private fun checkAndReloadDestinationsIfNeeded(newLocation: Location) {
        if (_uiState.value.isPreparingDestinations) return

        val lastLoadLocation = lastDestinationLoadLocation.get()
        if (lastLoadLocation == null) {
            startDestinationLoading(newLocation)
            return
        }

        val distanceKm = distanceCalculator.calculateKm(
            lat1 = lastLoadLocation.latitude,
            lon1 = lastLoadLocation.longitude,
            lat2 = newLocation.latitude,
            lon2 = newLocation.longitude,
        )
        if (distanceKm >= DESTINATION_RELOAD_THRESHOLD_KM) {
            startDestinationLoading(newLocation)
        }
    }

    private suspend fun checkAndRecalculateBoundsIfNeeded(newLocation: Location) {
        if (_uiState.value.isCalculatingBounds) return
        val lastLocation = lastBoundsCalculationLocation.get() ?: return
        val distanceKm = distanceCalculator.calculateKm(
            lat1 = lastLocation.latitude,
            lon1 = lastLocation.longitude,
            lat2 = newLocation.latitude,
            lon2 = newLocation.longitude,
        )
        if (distanceKm >= BOUNDS_RECALCULATION_THRESHOLD_KM) {
            calculateDistanceBounds(newLocation)
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

    private fun observeNutritionEvents() {
        viewModelScope.launch(dispatcherDefault) {
            observeNutritionBreakUseCase.observeNutritionBreakEvents().collect { event ->
                when (event) {
                    is NutritionBreakEvent.BreakRequired -> {
                        _uiState.update { it.copy(nutritionSuggestionTimeMs = event.suggestionTimeMs) }
                    }

                    NutritionBreakEvent.ChecksStopped -> {
                        _uiState.update { it.copy(nutritionSuggestionTimeMs = null) }
                    }
                }
            }
        }
    }

    private fun dismissNutritionPopup() {
        _uiState.update { it.copy(nutritionSuggestionTimeMs = null) }
    }

    private fun computeCurvePoints(userLocation: Location, destination: DestinationUiModel): List<LatLng> =
        createCurvePoints(
            start = Point(userLocation.latitude, userLocation.longitude),
            end = Point(destination.location.latitude, destination.location.longitude),
        ).map { LatLng(it.x, it.y) }
}
