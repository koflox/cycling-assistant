package com.koflox.connections.presentation.scanning

internal sealed interface BleScanningUiEvent {
    data object StartScan : BleScanningUiEvent
    data object StopScan : BleScanningUiEvent
    data class DeviceSelected(val macAddress: String, val name: String) : BleScanningUiEvent
    data object PermissionGranted : BleScanningUiEvent
    data object PermissionDenied : BleScanningUiEvent
}
