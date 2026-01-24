package com.koflox.session.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionParams
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.session.timer.SessionTimer
import com.koflox.session.presentation.session.timer.SessionTimerFactory
import com.koflox.session.service.SessionServiceController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class SessionViewModel(
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionServiceController: SessionServiceController,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
    sessionTimerFactory: SessionTimerFactory,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Idle)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<SessionNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private val sessionTimer: SessionTimer = sessionTimerFactory.create(viewModelScope)

    init {
        initialize()
    }

    private fun initialize() {
        showNotificationForStartedSession()
        observeActiveSession()
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
                    _uiState.value = SessionUiState.Idle
                }
            }
        }
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
                SessionUiEvent.PauseClicked -> pauseSession()
                SessionUiEvent.ResumeClicked -> resumeSession()
                SessionUiEvent.StopClicked -> showStopConfirmation()
                SessionUiEvent.StopConfirmationDismissed -> dismissStopConfirmation()
                SessionUiEvent.StopConfirmed -> confirmStop()
                SessionUiEvent.ErrorDismissed -> dismissError()
            }
        }
    }

    private fun showStopConfirmation() {
        updateActive { it.copy(overlay = SessionOverlay.StopConfirmation) }
    }

    private fun dismissStopConfirmation() {
        updateActive { it.copy(overlay = null) }
    }

    private suspend fun confirmStop() {
        val sessionId = (_uiState.value as? SessionUiState.Active)?.sessionId
        updateActive { it.copy(overlay = null) }
        stopSession()
        sessionId?.let { id ->
            _navigation.send(SessionNavigation.ToCompletion(id))
        }
    }

    fun startSession(
        destinationId: String,
        destinationName: String,
        destinationLatitude: Double,
        destinationLongitude: Double,
        startLatitude: Double,
        startLongitude: Double,
        startAltitudeMeters: Double?,
    ) {
        viewModelScope.launch(dispatcherDefault) {
            val hasActiveSession = activeSessionUseCase.observeActiveSession().first() != null
            if (hasActiveSession) return@launch

            createSessionUseCase.create(
                CreateSessionParams(
                    destinationId = destinationId,
                    destinationName = destinationName,
                    destinationLatitude = destinationLatitude,
                    destinationLongitude = destinationLongitude,
                    startLatitude = startLatitude,
                    startLongitude = startLongitude,
                    startAltitudeMeters = startAltitudeMeters,
                ),
            )
                .onSuccess { sessionServiceController.startSessionTracking() }
                .onFailure { showError(it) }
        }
    }

    private suspend fun pauseSession() {
        updateSessionStatusUseCase.pause()
            .onFailure { showError(it) }
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
            updateActive {
                it.copy(elapsedTimeFormatted = sessionUiMapper.formatElapsedTime(totalElapsedMs))
            }
        }
    }

    private fun stopTimer() {
        sessionTimer.stop()
    }

    private fun updateUiFromSession(session: Session) {
        val formattedData = sessionUiMapper.toSessionUiModel(session)
        val currentOverlay = (_uiState.value as? SessionUiState.Active)?.overlay
        _uiState.value = SessionUiState.Active(
            sessionId = session.id,
            destinationName = session.destinationName,
            destinationLocation = Location(
                latitude = session.destinationLatitude,
                longitude = session.destinationLongitude,
            ),
            status = session.status,
            elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
            traveledDistanceFormatted = formattedData.traveledDistanceFormatted,
            averageSpeedFormatted = formattedData.averageSpeedFormatted,
            topSpeedFormatted = formattedData.topSpeedFormatted,
            altitudeGainFormatted = formattedData.altitudeGainFormatted,
            currentLocation = session.trackPoints.lastOrNull()?.let {
                Location(it.latitude, it.longitude)
            },
            overlay = currentOverlay,
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
