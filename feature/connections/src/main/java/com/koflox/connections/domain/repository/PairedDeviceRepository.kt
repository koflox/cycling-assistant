package com.koflox.connections.domain.repository

import com.koflox.connections.domain.model.PairedDevice
import kotlinx.coroutines.flow.Flow

interface PairedDeviceRepository {
    fun observeAllDevices(): Flow<List<PairedDevice>>
    suspend fun getDeviceById(id: String): Result<PairedDevice?>
    suspend fun saveDevice(device: PairedDevice): Result<Unit>
    suspend fun deleteDevice(id: String): Result<Unit>
    suspend fun updateDevice(device: PairedDevice): Result<Unit>
}
