package com.koflox.destinations.data.source.local

import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PoiLocalDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val dao: DestinationDao,
) : PoiLocalDataSource {

    override suspend fun getAllDestinations(): List<DestinationLocal> = withContext(dispatcherIo) {
        dao.getAllDestinations()
    }

    override suspend fun insertAll(destinations: List<DestinationLocal>) = withContext(dispatcherIo) {
        dao.insertAll(destinations)
    }
}
