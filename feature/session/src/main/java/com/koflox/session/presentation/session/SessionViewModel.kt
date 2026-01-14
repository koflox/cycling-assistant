package com.koflox.session.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.location.LocationDataSource
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.SessionStartParams
import com.koflox.session.domain.usecase.SessionTransitionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class SessionViewModel(
    private val sessionTransitionUseCase: SessionTransitionUseCase,
    private val locationDataSource: LocationDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var activeSession: Session? = null
    private var locationTrackingJob: Job? = null
    private var timerJob: Job? = null

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
        onSessionEnded: () -> Unit,
    ) {
        if (activeSession != null) return
        viewModelScope.launch {
            sessionTransitionUseCase.start(
                SessionStartParams(
                    destinationId = destinationId,
                    destinationName = destinationName,
                    destinationLatitude = destinationLatitude,
                    destinationLongitude = destinationLongitude,
                    startLatitude = startLatitude,
                    startLongitude = startLongitude,
                ),
            ).onSuccess { session ->
                activeSession = session
                updateUiFromSession(session)
                startLocationTracking()
                startTimer()
                this@SessionViewModel.onSessionEnded = onSessionEnded
            }.onFailure { error ->
                // TODO: map error into human readable before displaying
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    private var onSessionEnded: (() -> Unit)? = null

    private fun pauseSession() {
        val session = activeSession ?: return
        val pausedSession = sessionTransitionUseCase.pause(session)
        activeSession = pausedSession
        updateUiFromSession(pausedSession)
        stopLocationTracking()
        stopTimer()
    }

    // TODO: fix time resuming after pausing, idle time shouldn't be included
    private fun resumeSession() {
        val session = activeSession ?: return
        val resumedSession = sessionTransitionUseCase.resume(session)
        activeSession = resumedSession
        updateUiFromSession(resumedSession)
        startLocationTracking()
        startTimer()
    }

    private fun stopSession() {
        val session = activeSession ?: return
        viewModelScope.launch {
            sessionTransitionUseCase.stop(session)
                .onSuccess {
                    cleanup()
                    onSessionEnded?.invoke()
                }
                .onFailure { error ->
                    // TODO: map error into human readable before displaying
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    private fun cleanup() {
        stopLocationTracking()
        stopTimer()
        activeSession = null
        _uiState.value = SessionUiState()
        onSessionEnded = null
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun startLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = viewModelScope.launch {
            while (isActive) {
                delay(LOCATION_TRACKING_INTERVAL_MS)
                if (activeSession?.status == SessionStatus.RUNNING) {
                    locationDataSource.getCurrentLocation()
                        .onSuccess { location ->
                            updateLocation(location)
                        }
                }
            }
        }
    }

    private fun stopLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
    }

    private fun updateLocation(location: Location) {
        val session = activeSession ?: return
        val updatedSession = sessionTransitionUseCase.updateLocation(
            session = session,
            latitude = location.latitude,
            longitude = location.longitude,
            timestampMs = System.currentTimeMillis(),
        )
        activeSession = updatedSession
        updateUiFromSession(updatedSession)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_UPDATE_INTERVAL_MS)
                activeSession?.let { session ->
                    if (session.status == SessionStatus.RUNNING) {
                        val elapsedMs = System.currentTimeMillis() - session.startTimeMs
                        _uiState.update {
                            it.copy(elapsedTimeFormatted = formatElapsedTime(elapsedMs))
                        }
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
        _uiState.update { currentState ->
            currentState.copy(
                isActive = true,
                destinationName = session.destinationName,
                destinationLocation = Location(
                    latitude = session.destinationLatitude,
                    longitude = session.destinationLongitude,
                ),
                status = session.status,
                elapsedTimeFormatted = formatElapsedTime(session.elapsedTimeMs),
                traveledDistanceKm = formatDistance(session.traveledDistanceKm),
                averageSpeedKmh = formatSpeed(session.averageSpeedKmh),
                topSpeedKmh = formatSpeed(session.topSpeedKmh),
                currentLocation = session.trackPoints.lastOrNull()?.let {
                    Location(it.latitude, it.longitude)
                },
            )
        }
    }

    private fun formatElapsedTime(elapsedMs: Long): String {
        val totalSeconds = elapsedMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatDistance(distanceKm: Double): String =
        String.format(Locale.getDefault(), "%.2f", distanceKm)

    private fun formatSpeed(speedKmh: Double): String =
        String.format(Locale.getDefault(), "%.1f", speedKmh)

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    companion object {
        private const val LOCATION_TRACKING_INTERVAL_MS = 3000L
        private const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }
}
