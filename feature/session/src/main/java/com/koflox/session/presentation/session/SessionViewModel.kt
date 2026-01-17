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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SessionViewModel(
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionServiceController: SessionServiceController,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
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

    fun onEvent(event: SessionUiEvent) {
        when (event) {
            SessionUiEvent.PauseClicked -> pauseSession()
            SessionUiEvent.ResumeClicked -> resumeSession()
            SessionUiEvent.StopClicked -> stopSession()
            SessionUiEvent.ErrorDismissed -> dismissError()
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
        viewModelScope.launch {
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
                .onFailure(::showError)
        }
    }

    private fun pauseSession() {
        viewModelScope.launch {
            updateSessionStatusUseCase.pause()
                .onFailure(::showError)
        }
    }

    private fun resumeSession() {
        viewModelScope.launch {
            updateSessionStatusUseCase.resume()
                .onFailure(::showError)
        }
    }

    private fun stopSession() {
        viewModelScope.launch {
            updateSessionStatusUseCase.stop()
                .onFailure(::showError)
        }
    }

    private fun showError(error: Throwable) {
        viewModelScope.launch {
            val message = errorMessageMapper.map(error)
            _uiState.update { it.copy(error = message) }
        }
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
