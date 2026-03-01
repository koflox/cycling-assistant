package com.koflox.connections.presentation.scanning

import com.koflox.designsystem.text.UiText

internal sealed interface BleScanningUiState {
    data object PermissionRequired : BleScanningUiState
    data class Scanning(val devices: List<ScannedDeviceUiModel>) : BleScanningUiState
    data class Error(val message: UiText) : BleScanningUiState
}

internal data class ScannedDeviceUiModel(
    val macAddress: String,
    val name: String,
    val isAlreadyPaired: Boolean,
)
