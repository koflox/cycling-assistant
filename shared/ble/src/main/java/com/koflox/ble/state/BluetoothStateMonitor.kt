package com.koflox.ble.state

import kotlinx.coroutines.flow.Flow

interface BluetoothStateMonitor {
    fun observeBluetoothEnabled(): Flow<Boolean>
}
