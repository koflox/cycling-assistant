package com.koflox.connections.data.source

import com.koflox.connections.data.source.local.entity.PairedDeviceEntity
import kotlinx.coroutines.flow.Flow

internal interface PairedDeviceLocalDataSource {
    fun observeAllDevices(): Flow<List<PairedDeviceEntity>>
    suspend fun getDeviceById(id: String): PairedDeviceEntity?
    suspend fun insertDevice(device: PairedDeviceEntity)
    suspend fun deleteDevice(id: String)
    suspend fun updateDevice(device: PairedDeviceEntity)
}
