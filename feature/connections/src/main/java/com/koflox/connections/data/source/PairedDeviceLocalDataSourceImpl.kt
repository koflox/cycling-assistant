package com.koflox.connections.data.source

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.connections.data.source.local.dao.PairedDeviceDao
import com.koflox.connections.data.source.local.entity.PairedDeviceEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class PairedDeviceLocalDataSourceImpl(
    private val daoFactory: ConcurrentFactory<PairedDeviceDao>,
    private val dispatcherIo: CoroutineDispatcher,
) : PairedDeviceLocalDataSource {

    override fun observeAllDevices(): Flow<List<PairedDeviceEntity>> = flow {
        emitAll(daoFactory.get().observeAllDevices())
    }.flowOn(dispatcherIo)

    override suspend fun getDeviceById(id: String): PairedDeviceEntity? = withContext(dispatcherIo) {
        daoFactory.get().getDeviceById(id)
    }

    override suspend fun insertDevice(device: PairedDeviceEntity): Unit = withContext(dispatcherIo) {
        daoFactory.get().insertDevice(device)
    }

    override suspend fun deleteDevice(id: String): Unit = withContext(dispatcherIo) {
        daoFactory.get().deleteDevice(id)
    }

    override suspend fun updateDevice(device: PairedDeviceEntity): Unit = withContext(dispatcherIo) {
        daoFactory.get().updateDevice(device)
    }
}
