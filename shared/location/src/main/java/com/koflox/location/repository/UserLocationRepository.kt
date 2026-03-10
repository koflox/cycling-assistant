package com.koflox.location.repository

import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

internal interface UserLocationRepository {
    suspend fun getUserLocation(): Result<Location>
    fun observeUserLocation(intervalMs: Long, minUpdateDistanceMeters: Float, maxUpdateDelayMs: Long = 0L): Flow<Location>
}
