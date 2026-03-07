package com.koflox.sensor.power.presentation.testmode

import com.koflox.designsystem.text.UiText

internal sealed interface PowerTestModeUiState {
    val overlay: PowerTestModeOverlay?
    data object Connecting : PowerTestModeUiState {
        override val overlay: PowerTestModeOverlay? = null
    }
    data class Connected(
        val currentPowerWatts: Int,
        val currentCadenceRpm: Float?,
        val sensorStats: List<PowerTestStatItem>,
        val recentReadings: List<PowerReadingUiModel>,
    ) : PowerTestModeUiState {
        override val overlay: PowerTestModeOverlay? = null
    }
    data class Disconnected(
        val message: UiText,
        override val overlay: PowerTestModeOverlay? = null,
    ) : PowerTestModeUiState
    data class Error(
        val message: UiText,
        override val overlay: PowerTestModeOverlay? = null,
    ) : PowerTestModeUiState
}

internal sealed interface PowerTestModeOverlay {
    data object BluetoothDisabled : PowerTestModeOverlay
}

internal data class PowerTestStatItem(
    val label: UiText,
    val value: String,
    val unit: UiText,
)

internal data class PowerReadingUiModel(
    val timestampMs: Long,
    val powerWatts: Int,
)
