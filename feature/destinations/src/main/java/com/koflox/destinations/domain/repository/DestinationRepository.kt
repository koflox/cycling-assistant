package com.koflox.destinations.domain.repository

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

internal interface DestinationRepository {
    fun loadDestinationsForLocation(location: Location): Flow<DestinationLoadingEvent>
    suspend fun getAllDestinations(): Result<List<Destination>>
    suspend fun getDestinationById(id: String): Result<Destination?>
    suspend fun getUserLocation(): Result<Location>
    fun observeUserLocation(): Flow<Location>
}
