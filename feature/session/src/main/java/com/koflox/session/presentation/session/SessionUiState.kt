package com.koflox.session.presentation.session

import com.koflox.location.model.Location
import com.koflox.session.domain.model.SessionStatus

data class SessionUiState(
    val isActive: Boolean = false,
    val destinationName: String = "",
    val destinationLocation: Location? = null,
    val status: SessionStatus = SessionStatus.RUNNING,
    val elapsedTimeFormatted: String = DEFAULT_TIME,
    val traveledDistanceKm: String = DEFAULT_DISTANCE,
    val averageSpeedKmh: String = DEFAULT_SPEED,
    val topSpeedKmh: String = DEFAULT_SPEED,
    val currentLocation: Location? = null,
    val error: String? = null,
) {
    val isPaused: Boolean get() = status == SessionStatus.PAUSED

    companion object {
        private const val DEFAULT_TIME = "00:00:00"
        private const val DEFAULT_DISTANCE = "0.00"
        private const val DEFAULT_SPEED = "0.0"
    }
}
