package com.koflox.destinations.domain.repository

import com.koflox.destinations.domain.model.Destination
import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

internal interface DestinationRepository {
    suspend fun initializeDatabase(): Result<Unit>
    suspend fun getAllDestinations(): Result<List<Destination>>
    suspend fun getUserLocation(): Result<Location>
    fun observeUserLocation(): Flow<Location>
}
