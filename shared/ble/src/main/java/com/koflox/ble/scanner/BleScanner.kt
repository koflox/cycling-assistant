package com.koflox.ble.scanner

import com.koflox.ble.model.BleDevice
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface BleScanner {
    fun scan(serviceUuids: List<UUID>, timeoutMs: Long): Flow<BleDevice>
}
