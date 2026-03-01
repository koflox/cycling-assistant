package com.koflox.ble.model

import java.util.UUID

data class BleDevice(
    val address: String,
    val name: String,
    val serviceUuids: List<UUID>,
)
