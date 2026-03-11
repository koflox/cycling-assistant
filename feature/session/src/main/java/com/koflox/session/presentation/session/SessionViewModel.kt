package com.koflox.session.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.di.SessionErrorMapper
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.model.Location
import com.koflox.location.usecase.CheckLocationEnabledUseCase
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionParams
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.model.DisplayStat
import com.koflox.session.presentation.session.timer.SessionTimer
import com.koflox.session.presentation.session.timer.SessionTimerFactory
import com.koflox.session.service.DeviceConnectionInfo
import com.koflox.session.service.PendingSessionAction
import com.koflox.session.service.PendingSessionActionConsumer
import com.koflox.session.service.PowerConnectionState
import com.koflox.session.service.PowerConnectionStateHolder
import com.koflox.session.service.SessionServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SessionViewModel @Inject constructor(
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase,
    private val sessionServiceController: SessionServiceController,
    private val pendingSessionAction: PendingSessionAction,
    private val pendingSessionActionConsumer: PendingSessionActionConsumer,
    private val sessionUiMapper: SessionUiMapper,
    @param:SessionErrorMapper private val errorMessageMapper: ErrorMessageMapper,
    private val powerConnectionStateHolder: PowerConnectionStateHolder,
    sessionTimerFactory: SessionTimerFactory,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Idle)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<SessionNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private val sessionTimer: SessionTimer = sessionTimerFactory.create(viewModelScope)

    private var isPausedDueToLocation = false
    private var activeStatsConfig: List<SessionStatType> = StatsDisplayConfig.DEFAULT_ACTIVE_SESSION_STATS

    init {
        initialize()
    }

    private fun initialize() {
        showNotificationForStartedSession()
        observeActiveSession()
        observeLocationEnabled()
        observeStopRequest()
        observeStatsConfig()
        observeDeviceConnectionState()
    }

    private fun observeActiveSession() {
        viewModelScope.launch(dispatcherDefault) {
            activeSessionUseCase.observeActiveSession().collect { session ->
                if (session != null) {
                    updateUiFromSession(session)
                    if (session.status == SessionStatus.RUNNING) {
                        startTimer(session)
                    } else {
                        stopTimer()
                    }
                } else {
                    stopTimer()
                    if (_uiState.value is SessionUiState.Active) {
                        pendingSessionActionConsumer.consumeStopRequest()
                    }
                    _uiState.value = SessionUiState.Idle
                }
            }
        }
    }

    private fun observeLocationEnabled() {
        viewModelScope.launch(dispatcherDefault) {
            checkLocationEnabledUseCase.observeLocationEnabled().collect { isEnabled ->
                val state = _uiState.value as? SessionUiState.Active ?: return@collect
                updateActive { it.copy(isLocationDisabled = !isEnabled) }
                if (!isEnabled && state.status == SessionStatus.RUNNING) {
                    isPausedDueToLocation = true
                } else if (isEnabled && isPausedDueToLocation) {
                    isPausedDueToLocation = false
                    resumeSession()
                }
            }
        }
    }

    private fun observeStopRequest() {
        viewModelScope.launch(dispatcherDefault) {
            pendingSessionAction.isStopRequested.collect { requested ->
                if (requested && _uiState.value is SessionUiState.Active) {
                    showStopConfirmation()
                }
            }
        }
    }

    private fun observeStatsConfig() {
        viewModelScope.launch(dispatcherDefault) {
            observeStatsDisplayConfigUseCase.observeActiveSessionStats().collect { config ->
                activeStatsConfig = config
                val session = activeSessionUseCase.observeActiveSession().first() ?: return@collect
                updateActive { current ->
                    current.copy(stats = sessionUiMapper.buildActiveSessionStats(session, activeStatsConfig))
                }
            }
        }
    }

    private fun observeDeviceConnectionState() {
        viewModelScope.launch(dispatcherDefault) {
            powerConnectionStateHolder.deviceConnectionInfo.collect { info ->
                updateActive { current ->
                    current.copy(
                        deviceStripItems = info?.let {
                            listOf(mapToDeviceStripItem(it))
                        } ?: emptyList()
                    )
                }
            }
        }
    }

    private fun mapToDeviceStripItem(info: DeviceConnectionInfo): DeviceStripItem {
        val stripState = when (val state = info.state) {
            is PowerConnectionState.Connected -> DeviceStripState.Connected(state.instantaneousPowerWatts)
            PowerConnectionState.Connecting -> DeviceStripState.Connecting
            is PowerConnectionState.Reconnecting -> DeviceStripState.Reconnecting(state.remaining)
        }
        return DeviceStripItem(deviceName = info.deviceName, state = stripState)
    }

    private fun showNotificationForStartedSession() {
        viewModelScope.launch(dispatcherDefault) {
            activeSessionUseCase.observeActiveSession()
                .firstOrNull()?.run {
                    sessionServiceController.startSessionTracking()
                }
        }
    }

    fun onEvent(event: SessionUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is SessionUiEvent.SessionManagementEvent -> handleSessionManagementEvent(event)
                is SessionUiEvent.LocationSettingsEvent -> handleLocationSettingsEvent(event)
                is SessionUiEvent.DeviceEvent -> handleDeviceEvent(event)
            }
        }
    }

    private suspend fun handleSessionManagementEvent(event: SessionUiEvent.SessionManagementEvent) {
        when (event) {
            SessionUiEvent.SessionManagementEvent.PauseClicked -> pauseSession()
            SessionUiEvent.SessionManagementEvent.ResumeClicked -> handleResumeClicked()
            SessionUiEvent.SessionManagementEvent.StopClicked -> showStopConfirmation()
            SessionUiEvent.SessionManagementEvent.StopConfirmationDismissed -> dismissStopConfirmation()
            SessionUiEvent.SessionManagementEvent.StopConfirmed -> confirmStop()
            SessionUiEvent.SessionManagementEvent.ErrorDismissed -> dismissError()
        }
    }

    private fun handleLocationSettingsEvent(event: SessionUiEvent.LocationSettingsEvent) {
        when (event) {
            SessionUiEvent.LocationSettingsEvent.EnableLocationClicked -> {
                updateActive { it.copy(overlay = SessionOverlay.LocationDisabled) }
            }

            SessionUiEvent.LocationSettingsEvent.LocationEnabled -> {
                updateActive { it.copy(overlay = null) }
            }

            SessionUiEvent.LocationSettingsEvent.LocationEnableDenied -> {
                updateActive { it.copy(overlay = null) }
            }

            SessionUiEvent.LocationSettingsEvent.LocationDisabledDismissed -> {
                updateActive { it.copy(overlay = null) }
            }
        }
    }

    private suspend fun handleDeviceEvent(event: SessionUiEvent.DeviceEvent) {
        when (event) {
            SessionUiEvent.DeviceEvent.StripClicked -> _navigation.send(SessionNavigation.ToConnections)
        }
    }

    private fun showStopConfirmation() {
        updateActive { it.copy(overlay = SessionOverlay.StopConfirmation) }
    }

    private fun dismissStopConfirmation() {
        pendingSessionActionConsumer.consumeStopRequest()
        updateActive { it.copy(overlay = null) }
    }

    private suspend fun confirmStop() {
        pendingSessionActionConsumer.consumeStopRequest()
        val sessionId = (_uiState.value as? SessionUiState.Active)?.sessionId
        updateActive { it.copy(overlay = null) }
        stopSession()
        sessionId?.let { id ->
            _navigation.send(SessionNavigation.ToCompletion(id))
        }
    }

    fun startSession(params: CreateSessionParams) {
        viewModelScope.launch(dispatcherDefault) {
            val hasActiveSession = activeSessionUseCase.observeActiveSession().first() != null
            if (hasActiveSession) return@launch
            createSessionUseCase.create(params)
                .onSuccess { sessionServiceController.startSessionTracking() }
                .onFailure { showError(it) }
        }
    }

    private suspend fun pauseSession() {
        isPausedDueToLocation = false
        updateSessionStatusUseCase.pause()
            .onFailure { showError(it) }
    }

    private suspend fun handleResumeClicked() {
        if (!checkLocationEnabledUseCase.isLocationEnabled()) {
            updateActive { it.copy(overlay = SessionOverlay.LocationDisabled) }
            return
        }
        resumeSession()
    }

    private suspend fun resumeSession() {
        updateSessionStatusUseCase.resume()
            .onFailure { showError(it) }
    }

    private suspend fun stopSession() {
        updateSessionStatusUseCase.stop()
            .onFailure { showError(it) }
    }

    private suspend fun showError(error: Throwable) {
        val message = errorMessageMapper.map(error)
        updateActive { it.copy(overlay = SessionOverlay.Error(message)) }
    }

    private fun dismissError() {
        updateActive { it.copy(overlay = null) }
    }

    private fun startTimer(session: Session) {
        sessionTimer.start(session) { totalElapsedMs ->
            val formattedTime = sessionUiMapper.formatElapsedTime(totalElapsedMs)
            updateActive {
                it.copy(
                    elapsedTimeFormatted = formattedTime,
                    stats = updateTimeStat(it.stats, formattedTime),
                )
            }
        }
    }

    private fun updateTimeStat(stats: List<DisplayStat>, formattedTime: String): List<DisplayStat> {
        val timeIndex = activeStatsConfig.indexOf(SessionStatType.TIME)
        if (timeIndex < 0 || timeIndex >= stats.size) return stats
        return stats.toMutableList().apply {
            this[timeIndex] = this[timeIndex].copy(value = formattedTime)
        }
    }

    private fun stopTimer() {
        sessionTimer.stop()
    }

    private fun updateUiFromSession(session: Session) {
        val formattedData = sessionUiMapper.toSessionUiModel(session)
        val currentOverlay = (_uiState.value as? SessionUiState.Active)?.overlay
            ?: if (pendingSessionAction.isStopRequested.value) SessionOverlay.StopConfirmation else null
        val currentDeviceStripItems = (_uiState.value as? SessionUiState.Active)?.deviceStripItems ?: emptyList()
        val destinationLocation = if (session.destinationLatitude != null && session.destinationLongitude != null) {
            Location(latitude = session.destinationLatitude, longitude = session.destinationLongitude)
        } else {
            null
        }
        _uiState.value = SessionUiState.Active(
            sessionId = session.id,
            destinationName = session.destinationName,
            destinationLocation = destinationLocation,
            status = session.status,
            elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
            traveledDistanceFormatted = formattedData.traveledDistanceFormatted,
            averageSpeedFormatted = formattedData.averageSpeedFormatted,
            topSpeedFormatted = formattedData.topSpeedFormatted,
            altitudeGainFormatted = formattedData.altitudeGainFormatted,
            stats = sessionUiMapper.buildActiveSessionStats(session, activeStatsConfig),
            currentLocation = session.trackPoints.lastOrNull()?.let {
                Location(it.latitude, it.longitude)
            },
            isLocationDisabled = !checkLocationEnabledUseCase.isLocationEnabled(),
            overlay = currentOverlay,
            deviceStripItems = currentDeviceStripItems,
        )
    }

    private inline fun updateActive(transform: (SessionUiState.Active) -> SessionUiState.Active) {
        val current = _uiState.value
        if (current is SessionUiState.Active) {
            _uiState.value = transform(current)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
