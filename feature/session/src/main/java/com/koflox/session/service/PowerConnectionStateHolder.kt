package com.koflox.session.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface PowerConnectionStateHolder {
    val deviceConnectionInfo: StateFlow<DeviceConnectionInfo?>
}

internal interface PowerConnectionStatePublisher {
    fun updateState(info: DeviceConnectionInfo?)
}

internal class PowerConnectionStateHolderImpl : PowerConnectionStateHolder, PowerConnectionStatePublisher {

    private val _deviceConnectionInfo = MutableStateFlow<DeviceConnectionInfo?>(null)
    override val deviceConnectionInfo: StateFlow<DeviceConnectionInfo?> = _deviceConnectionInfo.asStateFlow()

    override fun updateState(info: DeviceConnectionInfo?) {
        _deviceConnectionInfo.value = info
    }
}
