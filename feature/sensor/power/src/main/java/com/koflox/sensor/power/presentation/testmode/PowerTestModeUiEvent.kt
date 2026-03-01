package com.koflox.sensor.power.presentation.testmode

internal sealed interface PowerTestModeUiEvent {
    data object Disconnect : PowerTestModeUiEvent
    data object Reconnect : PowerTestModeUiEvent
    data object BluetoothDisabledDismissed : PowerTestModeUiEvent
}
