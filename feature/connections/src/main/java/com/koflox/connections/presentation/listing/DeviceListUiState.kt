package com.koflox.connections.presentation.listing

import com.koflox.connections.domain.model.DeviceType
import com.koflox.designsystem.text.UiText

internal sealed interface DeviceListUiState {
    val overlay: DeviceListOverlay?
    data object Loading : DeviceListUiState {
        override val overlay: DeviceListOverlay? = null
    }
    data class Empty(
        override val overlay: DeviceListOverlay? = null,
    ) : DeviceListUiState
    data class Content(
        val devices: List<DeviceListItemUiModel>,
        override val overlay: DeviceListOverlay? = null,
    ) : DeviceListUiState
}

internal sealed interface DeviceListOverlay {
    data class Error(val message: UiText) : DeviceListOverlay
    data object BluetoothDisabled : DeviceListOverlay
    data class DeleteConfirmation(val deviceId: String, val deviceName: String) : DeviceListOverlay
}

internal data class DeviceListItemUiModel(
    val id: String,
    val name: String,
    val macAddress: String,
    val deviceType: DeviceType,
    val isSessionUsageEnabled: Boolean,
)
