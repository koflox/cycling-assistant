package com.koflox.destinations.presentation.destinations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.destinationnutrition.bridge.model.NutritionBreakEvent
import com.koflox.destinationnutrition.bridge.usecase.ObserveNutritionBreakUseCase
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.usecase.CheckLocationEnabledUseCase
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.ObserveRidingModeUseCase
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.domain.usecase.ToleranceCalculator
import com.koflox.destinations.domain.usecase.UpdateRidingModeUseCase
import com.koflox.destinations.presentation.destinations.delegate.DestinationDelegate
import com.koflox.destinations.presentation.destinations.delegate.LocationDelegate
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RideMapViewModel(
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase,
    private val getDestinationInfoUseCase: GetDestinationInfoUseCase,
    private val getDistanceBoundsUseCase: GetDistanceBoundsUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiMapper: DestinationUiMapper,
    private val toleranceCalculator: ToleranceCalculator,
    private val application: Application,
    private val cyclingSessionUseCase: CyclingSessionUseCase,
    private val observeNutritionBreakUseCase: ObserveNutritionBreakUseCase,
    private val observeRidingModeUseCase: ObserveRidingModeUseCase,
    private val updateRidingModeUseCase: UpdateRidingModeUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : AndroidViewModel(application) {

    private val _internalState = MutableStateFlow(RideMapInternalState())

    val uiState: StateFlow<RideMapUiState> = _internalState
        .map { deriveUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RideMapUiState.Loading)

    private var isScreenVisible = false

    private val locationDelegate by lazy {
        LocationDelegate(
            getUserLocationUseCase = getUserLocationUseCase,
            observeUserLocationUseCase = observeUserLocationUseCase,
            distanceCalculator = distanceCalculator,
            uiState = _internalState,
            scope = viewModelScope,
            onLocationUpdated = ::onLocationUpdated,
        )
    }

    private val destinationDelegate by lazy {
        DestinationDelegate(
            initializeDatabaseUseCase = initializeDatabaseUseCase,
            getDestinationInfoUseCase = getDestinationInfoUseCase,
            getDistanceBoundsUseCase = getDistanceBoundsUseCase,
            getUserLocationUseCase = getUserLocationUseCase,
            distanceCalculator = distanceCalculator,
            uiMapper = uiMapper,
            toleranceCalculator = toleranceCalculator,
            application = application,
            uiState = _internalState,
            scope = viewModelScope,
        )
    }

    init {
        initialize()
    }

    private fun initialize() {
        observeLocationEnabled()
        observeRidingMode()
        initializeInternal()
    }

    private fun initializeInternal() {
        viewModelScope.launch(dispatcherDefault) {
            _internalState.update { it.copy(isInitializing = true) }
            val location = locationDelegate.fetchInitialLocation()
            if (location != null && _internalState.value.ridingMode == RidingMode.DESTINATION) {
                destinationDelegate.startDestinationLoading(location)
            }
            checkActiveSession()
            _internalState.update { it.copy(isInitializing = false) }
            listenToActiveSession()
            observeNutritionEvents()
        }
    }

    private fun observeLocationEnabled() {
        viewModelScope.launch(dispatcherDefault) {
            checkLocationEnabledUseCase.observeLocationEnabled().collect { isEnabled ->
                _internalState.update { it.copy(isLocationDisabled = !isEnabled) }
            }
        }
    }

    private fun observeRidingMode() {
        viewModelScope.launch(dispatcherDefault) {
            observeRidingModeUseCase.observe().collect { mode ->
                _internalState.update { it.copy(ridingMode = mode) }
            }
        }
    }

    private suspend fun checkActiveSession() {
        val isActive = cyclingSessionUseCase.observeHasActiveSession().first()
        val activeDestination = if (isActive) cyclingSessionUseCase.getActiveSessionDestination() else null
        _internalState.update {
            it.copy(
                isActiveSessionChecked = true,
                isSessionActive = isActive,
            )
        }
        activeDestination?.let { destinationDelegate.findDestination(destinationId = it.id) }
    }

    private fun listenToActiveSession() {
        viewModelScope.launch(dispatcherDefault) {
            cyclingSessionUseCase.observeHasActiveSession().collect { isActive ->
                _internalState.update {
                    if (isActive) {
                        it.copy(isSessionActive = true)
                    } else {
                        it.copy(
                            isSessionActive = false,
                            selectedDestination = null,
                            curvePoints = emptyList(),
                            showSelectedMarkerOptionsDialog = false,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: RideMapUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is RideMapUiEvent.ModeEvent -> handleModeEvent(event)
                is RideMapUiEvent.LifecycleEvent -> handleLifecycleEvent(event)
                is RideMapUiEvent.PermissionEvent -> handlePermissionEvent(event)
                is RideMapUiEvent.DestinationEvent -> handleDestinationEvent(event)
                is RideMapUiEvent.SessionEvent -> handleSessionEvent(event)
                is RideMapUiEvent.CommonEvent -> handleCommonEvent(event)
            }
        }
    }

    private suspend fun handleModeEvent(event: RideMapUiEvent.ModeEvent) {
        when (event) {
            is RideMapUiEvent.ModeEvent.ModeSelected -> onModeSelected(event.mode)
        }
    }

    private fun handleLifecycleEvent(event: RideMapUiEvent.LifecycleEvent) {
        when (event) {
            RideMapUiEvent.LifecycleEvent.ScreenResumed -> onScreenResumed()
            RideMapUiEvent.LifecycleEvent.ScreenPaused -> onScreenPaused()
            RideMapUiEvent.LifecycleEvent.RetryInitializationClicked -> initializeInternal()
        }
    }

    private fun handlePermissionEvent(event: RideMapUiEvent.PermissionEvent) {
        when (event) {
            RideMapUiEvent.PermissionEvent.PermissionGranted -> onPermissionGranted()
            RideMapUiEvent.PermissionEvent.PermissionDenied -> onPermissionDenied()
        }
    }

    private suspend fun handleDestinationEvent(event: RideMapUiEvent.DestinationEvent) {
        when (event) {
            is RideMapUiEvent.DestinationEvent.RouteDistanceChanged -> destinationDelegate.updateRouteDistance(event.distanceKm)
            RideMapUiEvent.DestinationEvent.LetsGoClicked -> destinationDelegate.findDestination()
            is RideMapUiEvent.DestinationEvent.OpenInGoogleMaps -> destinationDelegate.openInGoogleMaps(event.destination)
            RideMapUiEvent.DestinationEvent.SelectedMarkerInfoClicked -> destinationDelegate.showMarkerOptionsDialog()
            RideMapUiEvent.DestinationEvent.SelectedMarkerOptionsDialogDismissed -> destinationDelegate.dismissSelectedMarkerOptionsDialog()
        }
    }

    private suspend fun handleSessionEvent(event: RideMapUiEvent.SessionEvent) {
        when (event) {
            RideMapUiEvent.SessionEvent.StartFreeRoamClicked -> startFreeRoamSession()
        }
    }

    private fun handleCommonEvent(event: RideMapUiEvent.CommonEvent) {
        when (event) {
            RideMapUiEvent.CommonEvent.ErrorDismissed -> _internalState.update { it.copy(error = null) }
            RideMapUiEvent.CommonEvent.NavigationActionHandled -> _internalState.update { it.copy(navigationAction = null) }
            RideMapUiEvent.CommonEvent.NutritionPopupDismissed -> _internalState.update {
                it.copy(nutritionSuggestionTimeMs = null)
            }
        }
    }

    private suspend fun onModeSelected(mode: RidingMode) {
        updateRidingModeUseCase.update(mode)
        val location = _internalState.value.userLocation
        if (mode == RidingMode.DESTINATION && location != null && !_internalState.value.areDestinationsReady) {
            destinationDelegate.startDestinationLoading(location)
        }
    }

    private fun onPermissionGranted() {
        _internalState.update { it.copy(isPermissionGranted = true) }
        fetchInitialLocationAndStartLoading()
        if (isScreenVisible) {
            locationDelegate.startLocationObservation()
        }
    }

    private fun fetchInitialLocationAndStartLoading() {
        viewModelScope.launch(dispatcherDefault) {
            val location = locationDelegate.fetchInitialLocation()
            if (location != null && _internalState.value.ridingMode == RidingMode.DESTINATION) {
                destinationDelegate.checkAndReloadDestinationsIfNeeded(location)
            }
        }
    }

    private fun onPermissionDenied() {
        _internalState.update { it.copy(error = application.getString(R.string.error_location_permission_denied)) }
    }

    private fun onScreenResumed() {
        isScreenVisible = true
        if (_internalState.value.isPermissionGranted) {
            locationDelegate.startLocationObservation()
        }
    }

    private fun onScreenPaused() {
        isScreenVisible = false
        locationDelegate.stopLocationObservation()
    }

    private fun onLocationUpdated(newLocation: Location) {
        if (_internalState.value.ridingMode == RidingMode.DESTINATION) {
            destinationDelegate.updateCurvePointsForLocation(newLocation)
            destinationDelegate.checkAndReloadDestinationsIfNeeded(newLocation)
            viewModelScope.launch(dispatcherDefault) {
                destinationDelegate.checkAndRecalculateBoundsIfNeeded(newLocation)
            }
        }
    }

    private suspend fun startFreeRoamSession() {
        _internalState.update { it.copy(isStartingFreeRoam = true) }
        cyclingSessionUseCase.startFreeRoamSession()
            .onFailure {
                _internalState.update {
                    it.copy(
                        isStartingFreeRoam = false,
                        error = application.getString(R.string.error_not_handled),
                    )
                }
            }
            .onSuccess {
                _internalState.update { it.copy(isStartingFreeRoam = false) }
            }
    }

    private fun observeNutritionEvents() {
        viewModelScope.launch(dispatcherDefault) {
            observeNutritionBreakUseCase.observeNutritionBreakEvents().collect { event ->
                when (event) {
                    is NutritionBreakEvent.BreakRequired -> {
                        _internalState.update { it.copy(nutritionSuggestionTimeMs = event.suggestionTimeMs) }
                    }
                    NutritionBreakEvent.ChecksStopped -> {
                        _internalState.update { it.copy(nutritionSuggestionTimeMs = null) }
                    }
                }
            }
        }
    }

    private fun deriveUiState(state: RideMapInternalState): RideMapUiState = when {
        state.isLocationRetryNeeded -> RideMapUiState.LocationDisabled
        state.isSessionActive && state.isReady -> RideMapUiState.ActiveSession(
            userLocation = state.userLocation,
            cameraFocusLocation = state.cameraFocusLocation,
            selectedDestination = if (state.isFreeRoam) null else state.selectedDestination,
            curvePoints = if (state.isFreeRoam) emptyList() else state.curvePoints,
            showSelectedMarkerOptionsDialog = if (state.isFreeRoam) false else state.showSelectedMarkerOptionsDialog,
            error = state.error,
            navigationAction = state.navigationAction,
            nutritionSuggestionTimeMs = state.nutritionSuggestionTimeMs,
        )
        state.isReady && state.isFreeRoam -> RideMapUiState.FreeRoamIdle(
            userLocation = state.userLocation,
            cameraFocusLocation = state.cameraFocusLocation,
            isStartingFreeRoam = state.isStartingFreeRoam,
            error = state.error,
        )
        state.isReady -> RideMapUiState.DestinationIdle(
            userLocation = state.userLocation,
            cameraFocusLocation = state.cameraFocusLocation,
            selectedDestination = state.selectedDestination,
            otherValidDestinations = state.otherValidDestinations,
            curvePoints = state.curvePoints,
            routeDistanceKm = state.routeDistanceKm,
            toleranceKm = state.toleranceKm,
            distanceBounds = state.distanceBounds,
            isPreparingDestinations = state.isPreparingDestinations,
            isCalculatingBounds = state.isCalculatingBounds,
            areDistanceBoundsReady = state.areDistanceBoundsReady,
            isLoading = state.isLoading,
            showSelectedMarkerOptionsDialog = state.showSelectedMarkerOptionsDialog,
            error = state.error,
            navigationAction = state.navigationAction,
        )
        else -> RideMapUiState.Loading
    }
}
