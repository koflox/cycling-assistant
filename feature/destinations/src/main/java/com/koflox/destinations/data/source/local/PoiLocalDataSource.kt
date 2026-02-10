package com.koflox.destinations.data.source.local

import com.koflox.destinations.data.source.local.entity.DestinationLocal

internal interface PoiLocalDataSource {
    suspend fun getDestinationsInArea(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): List<DestinationLocal>
    suspend fun getDestinationById(id: String): DestinationLocal?
    suspend fun insertAll(destinations: List<DestinationLocal>)
}
