package com.koflox.destinations.data.source.local

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PoiLocalDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val daoFactory: ConcurrentFactory<DestinationDao>,
) : PoiLocalDataSource {

    override suspend fun getDestinationsInArea(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
    ): List<DestinationLocal> = withContext(dispatcherIo) {
        daoFactory.get().getDestinationsInArea(minLat, maxLat, minLon, maxLon)
    }

    override suspend fun getDestinationById(id: String): DestinationLocal? = withContext(dispatcherIo) {
        daoFactory.get().getDestinationById(id)
    }

    override suspend fun insertAll(destinations: List<DestinationLocal>) = withContext(dispatcherIo) {
        daoFactory.get().insertAll(destinations)
    }
}
