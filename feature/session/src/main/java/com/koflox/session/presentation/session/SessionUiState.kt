package com.koflox.session.presentation.session

import com.koflox.designsystem.text.UiText
import com.koflox.location.model.Location
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.presentation.model.DisplayStat

internal sealed interface SessionUiState {
    data object Idle : SessionUiState

    data class Active(
        val sessionId: String,
        val destinationName: String?,
        val destinationLocation: Location?,
        val status: SessionStatus,
        val elapsedTimeFormatted: String,
        val traveledDistanceFormatted: String,
        val averageSpeedFormatted: String,
        val topSpeedFormatted: String,
        val altitudeGainFormatted: String,
        val stats: List<DisplayStat>,
        val currentLocation: Location?,
        val isLocationDisabled: Boolean = false,
        val overlay: SessionOverlay? = null,
        val powerDisplayState: PowerDisplayState = PowerDisplayState.None,
    ) : SessionUiState {
        val isPaused: Boolean get() = status == SessionStatus.PAUSED
    }
}

internal sealed interface PowerDisplayState {
    data object None : PowerDisplayState
    data object Connecting : PowerDisplayState
    data class Receiving(val avgPowerFormatted: String) : PowerDisplayState
}

internal sealed interface SessionOverlay {
    data object StopConfirmation : SessionOverlay
    data object LocationDisabled : SessionOverlay
    data class Error(val message: UiText) : SessionOverlay
}
