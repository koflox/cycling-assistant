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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RideMapViewModel(
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
    getUserLocationUseCase: GetUserLocationUseCase,
    observeUserLocationUseCase: ObserveUserLocationUseCase,
    initializeDatabaseUseCase: InitializeDatabaseUseCase,
    getDestinationInfoUseCase: GetDestinationInfoUseCase,
    getDistanceBoundsUseCase: GetDistanceBoundsUseCase,
    distanceCalculator: DistanceCalculator,
    uiMapper: DestinationUiMapper,
    toleranceCalculator: ToleranceCalculator,
    private val application: Application,
    private val cyclingSessionUseCase: CyclingSessionUseCase,
    private val observeNutritionBreakUseCase: ObserveNutritionBreakUseCase,
    private val observeRidingModeUseCase: ObserveRidingModeUseCase,
    private val updateRidingModeUseCase: UpdateRidingModeUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RideMapUiState())
    val uiState: StateFlow<RideMapUiState> = _uiState.asStateFlow()

    private var isScreenVisible = false

    private val locationDelegate = LocationDelegate(
        getUserLocationUseCase = getUserLocationUseCase,
        observeUserLocationUseCase = observeUserLocationUseCase,
        distanceCalculator = distanceCalculator,
        uiState = _uiState,
        scope = viewModelScope,
        onLocationUpdated = ::onLocationUpdated,
    )

    private val destinationDelegate = DestinationDelegate(
        initializeDatabaseUseCase = initializeDatabaseUseCase,
        getDestinationInfoUseCase = getDestinationInfoUseCase,
        getDistanceBoundsUseCase = getDistanceBoundsUseCase,
        getUserLocationUseCase = getUserLocationUseCase,
        distanceCalculator = distanceCalculator,
        uiMapper = uiMapper,
        toleranceCalculator = toleranceCalculator,
        application = application,
        uiState = _uiState,
        scope = viewModelScope,
    )

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
            _uiState.update { it.copy(isInitializing = true) }
            val location = locationDelegate.fetchInitialLocation()
            if (location != null && _uiState.value.ridingMode == RidingMode.DESTINATION) {
                destinationDelegate.startDestinationLoading(location)
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

    private fun observeRidingMode() {
        viewModelScope.launch(dispatcherDefault) {
            observeRidingModeUseCase.observe().collect { mode ->
                _uiState.update { it.copy(ridingMode = mode) }
            }
        }
    }

    private suspend fun checkActiveSession() {
        val isActive = cyclingSessionUseCase.observeHasActiveSession().first()
        val activeDestination = if (isActive) cyclingSessionUseCase.getActiveSessionDestination() else null
        _uiState.update {
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
                _uiState.update { it.copy(isSessionActive = isActive) }
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
            RideMapUiEvent.CommonEvent.ErrorDismissed -> _uiState.update { it.copy(error = null) }
            RideMapUiEvent.CommonEvent.NavigationActionHandled -> _uiState.update { it.copy(navigationAction = null) }
            RideMapUiEvent.CommonEvent.NutritionPopupDismissed -> _uiState.update { it.copy(nutritionSuggestionTimeMs = null) }
        }
    }

    private suspend fun onModeSelected(mode: RidingMode) {
        updateRidingModeUseCase.update(mode)
        val location = _uiState.value.userLocation
        if (mode == RidingMode.DESTINATION && location != null && !_uiState.value.areDestinationsReady) {
            destinationDelegate.startDestinationLoading(location)
        }
    }

    private fun onPermissionGranted() {
        _uiState.update { it.copy(isPermissionGranted = true) }
        fetchInitialLocationAndStartLoading()
        if (isScreenVisible) {
            locationDelegate.startLocationObservation()
        }
    }

    private fun fetchInitialLocationAndStartLoading() {
        viewModelScope.launch(dispatcherDefault) {
            val location = locationDelegate.fetchInitialLocation()
            if (location != null && _uiState.value.ridingMode == RidingMode.DESTINATION) {
                destinationDelegate.checkAndReloadDestinationsIfNeeded(location)
            }
        }
    }

    private fun onPermissionDenied() {
        _uiState.update { it.copy(error = application.getString(R.string.error_location_permission_denied)) }
    }

    private fun onScreenResumed() {
        isScreenVisible = true
        if (_uiState.value.isPermissionGranted) {
            locationDelegate.startLocationObservation()
        }
    }

    private fun onScreenPaused() {
        isScreenVisible = false
        locationDelegate.stopLocationObservation()
    }

    private fun onLocationUpdated(newLocation: Location) {
        if (_uiState.value.ridingMode == RidingMode.DESTINATION) {
            destinationDelegate.updateCurvePointsForLocation(newLocation)
            destinationDelegate.checkAndReloadDestinationsIfNeeded(newLocation)
            viewModelScope.launch(dispatcherDefault) {
                destinationDelegate.checkAndRecalculateBoundsIfNeeded(newLocation)
            }
        }
    }

    private suspend fun startFreeRoamSession() {
        _uiState.update { it.copy(isStartingFreeRoam = true) }
        cyclingSessionUseCase.startFreeRoamSession()
            .onFailure {
                _uiState.update {
                    it.copy(
                        isStartingFreeRoam = false,
                        error = application.getString(R.string.error_not_handled),
                    )
                }
            }
            .onSuccess {
                _uiState.update { it.copy(isStartingFreeRoam = false) }
            }
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
}
