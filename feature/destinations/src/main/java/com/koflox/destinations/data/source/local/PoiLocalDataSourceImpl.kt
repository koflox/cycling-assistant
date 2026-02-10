package com.koflox.destinations.data.source.local

import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PoiLocalDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val dao: DestinationDao,
) : PoiLocalDataSource {

    override suspend fun getDestinationsInArea(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
    ): List<DestinationLocal> = withContext(dispatcherIo) {
        dao.getDestinationsInArea(minLat, maxLat, minLon, maxLon)
    }

    override suspend fun getDestinationById(id: String): DestinationLocal? = withContext(dispatcherIo) {
        dao.getDestinationById(id)
    }

    override suspend fun insertAll(destinations: List<DestinationLocal>) = withContext(dispatcherIo) {
        dao.insertAll(destinations)
    }
}
