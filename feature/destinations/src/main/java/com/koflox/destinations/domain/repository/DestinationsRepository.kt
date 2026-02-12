package com.koflox.destinations.domain.repository

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

internal interface DestinationsRepository {
    fun loadDestinationsForLocation(location: Location): Flow<DestinationLoadingEvent>
    suspend fun getDestinationsInArea(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
    ): Result<List<Destination>>
    suspend fun getDestinationById(id: String): Result<Destination?>
}
