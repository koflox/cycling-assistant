package com.koflox.session.service

internal data class DeviceConnectionInfo(
    val deviceName: String,
    val state: PowerConnectionState,
)
