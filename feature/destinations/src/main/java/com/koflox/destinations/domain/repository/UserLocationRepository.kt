package com.koflox.destinations.domain.repository

import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

internal interface UserLocationRepository {
    suspend fun getUserLocation(): Result<Location>
    fun observeUserLocation(): Flow<Location>
}
