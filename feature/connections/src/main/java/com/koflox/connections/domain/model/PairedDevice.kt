package com.koflox.connections.domain.model

data class PairedDevice(
    val id: String,
    val macAddress: String,
    val name: String,
    val deviceType: DeviceType,
    val isSessionUsageEnabled: Boolean,
)
