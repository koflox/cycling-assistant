package com.koflox.connections.presentation.listing

internal sealed interface DeviceListUiEvent {
    data class ToggleSessionUsage(val id: String, val isEnabled: Boolean) : DeviceListUiEvent
    data class DeleteDeviceRequested(val id: String, val name: String) : DeviceListUiEvent
    data class DeleteDeviceConfirmed(val id: String) : DeviceListUiEvent
    data object DeleteDeviceDismissed : DeviceListUiEvent
    data object AddDeviceClicked : DeviceListUiEvent
    data class TestModeClicked(val macAddress: String) : DeviceListUiEvent
    data object ErrorDismissed : DeviceListUiEvent
    data object BluetoothDisabledDismissed : DeviceListUiEvent
}
