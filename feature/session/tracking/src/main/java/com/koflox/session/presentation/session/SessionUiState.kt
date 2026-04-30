package com.koflox.session.presentation.session

import com.koflox.designsystem.text.UiText
import com.koflox.location.model.Location
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.presentation.model.DisplayStat
import kotlin.time.Duration

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
        val deviceStripItems: List<DeviceStripItem> = emptyList(),
    ) : SessionUiState {
        val isPaused: Boolean get() = status == SessionStatus.PAUSED
    }
}

internal sealed interface DeviceStripState {
    data class Connected(val instantaneousPowerWatts: Int) : DeviceStripState
    data object Connecting : DeviceStripState
    data class Reconnecting(val remaining: Duration) : DeviceStripState
}

internal data class DeviceStripItem(
    val deviceName: String,
    val state: DeviceStripState,
)

internal sealed interface SessionOverlay {
    data object StopConfirmation : SessionOverlay
    data object LocationDisabled : SessionOverlay
    data class Error(val message: UiText) : SessionOverlay
}
