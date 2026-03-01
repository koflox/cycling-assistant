package com.koflox.connections.data.repository

import com.koflox.concurrent.suspendRunCatching
import com.koflox.connections.data.mapper.PairedDeviceMapper
import com.koflox.connections.data.source.PairedDeviceLocalDataSource
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.repository.PairedDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PairedDeviceRepositoryImpl(
    private val localDataSource: PairedDeviceLocalDataSource,
    private val mapper: PairedDeviceMapper,
) : PairedDeviceRepository {

    override fun observeAllDevices(): Flow<List<PairedDevice>> =
        localDataSource.observeAllDevices().map { entities ->
            entities.map(mapper::toDomain)
        }

    override suspend fun getDeviceById(id: String): Result<PairedDevice?> = suspendRunCatching {
        localDataSource.getDeviceById(id)?.let(mapper::toDomain)
    }

    override suspend fun saveDevice(device: PairedDevice): Result<Unit> = suspendRunCatching {
        localDataSource.insertDevice(mapper.toEntity(device))
    }

    override suspend fun deleteDevice(id: String): Result<Unit> = suspendRunCatching {
        localDataSource.deleteDevice(id)
    }

    override suspend fun updateDevice(device: PairedDevice): Result<Unit> = suspendRunCatching {
        localDataSource.updateDevice(mapper.toEntity(device))
    }
}
