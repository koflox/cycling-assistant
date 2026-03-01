package com.koflox.connections.presentation.listing

internal sealed interface DeviceListNavigation {
    data class ToTestMode(val macAddress: String) : DeviceListNavigation
    data object ToScanning : DeviceListNavigation
}
