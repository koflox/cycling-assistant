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
import com.koflox.session.service.SessionServiceController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SessionViewModel(
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionServiceController: SessionServiceController,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<SessionNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private var timerJob: Job? = null

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
                    _uiState.value = SessionUiState()
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
        _uiState.update { it.copy(showStopConfirmationDialog = true) }
    }

    private fun dismissStopConfirmation() {
        _uiState.update { it.copy(showStopConfirmationDialog = false) }
    }

    private suspend fun confirmStop() {
        val sessionId = _uiState.value.sessionId
        _uiState.update { it.copy(showStopConfirmationDialog = false) }
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
        _uiState.update { it.copy(error = message) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun startTimer(session: Session) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_UPDATE_INTERVAL_MS)
                if (session.status == SessionStatus.RUNNING) {
                    val elapsedSinceLastResume = System.currentTimeMillis() - session.lastResumedTimeMs
                    val totalElapsedMs = session.elapsedTimeMs + elapsedSinceLastResume
                    _uiState.update {
                        it.copy(elapsedTimeFormatted = sessionUiMapper.formatElapsedTime(totalElapsedMs))
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateUiFromSession(session: Session) {
        val formattedData = sessionUiMapper.toSessionUiModel(session)
        _uiState.update { currentState ->
            currentState.copy(
                isActive = true,
                sessionId = session.id,
                destinationName = session.destinationName,
                destinationLocation = Location(
                    latitude = session.destinationLatitude,
                    longitude = session.destinationLongitude,
                ),
                status = session.status,
                elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
                traveledDistanceKm = formattedData.traveledDistanceFormatted,
                averageSpeedKmh = formattedData.averageSpeedFormatted,
                topSpeedKmh = formattedData.topSpeedFormatted,
                currentLocation = session.trackPoints.lastOrNull()?.let {
                    Location(it.latitude, it.longitude)
                },
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    companion object {
        private const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }
}
