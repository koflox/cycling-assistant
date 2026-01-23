package com.koflox.session.presentation.session

import com.koflox.location.model.Location
import com.koflox.session.domain.model.SessionStatus

internal sealed interface SessionUiState {
    data object Idle : SessionUiState

    data class Active(
        val sessionId: String,
        val destinationName: String,
        val destinationLocation: Location,
        val status: SessionStatus,
        val elapsedTimeFormatted: String,
        val traveledDistanceFormatted: String,
        val averageSpeedFormatted: String,
        val topSpeedFormatted: String,
        val currentLocation: Location?,
        val overlay: SessionOverlay? = null,
    ) : SessionUiState {
        val isPaused: Boolean get() = status == SessionStatus.PAUSED
    }
}

internal sealed interface SessionOverlay {
    data object StopConfirmation : SessionOverlay
    data class Error(val message: String) : SessionOverlay
}
