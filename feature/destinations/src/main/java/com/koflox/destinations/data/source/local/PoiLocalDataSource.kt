package com.koflox.destinations.data.source.local

import com.koflox.destinations.data.source.local.entity.DestinationLocal

internal interface PoiLocalDataSource {
    suspend fun getAllDestinations(): List<DestinationLocal>
    suspend fun insertAll(destinations: List<DestinationLocal>)
}
