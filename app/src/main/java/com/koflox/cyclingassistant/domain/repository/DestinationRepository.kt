package com.koflox.cyclingassistant.domain.repository

import com.koflox.cyclingassistant.domain.model.Destination
import com.koflox.cyclingassistant.domain.model.Location

internal interface DestinationRepository {
    suspend fun initializeDatabase(): Result<Unit>
    suspend fun getAllDestinations(): Result<List<Destination>>
    suspend fun getUserLocation(): Result<Location>
}
