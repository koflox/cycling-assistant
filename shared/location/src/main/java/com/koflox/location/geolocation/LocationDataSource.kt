package com.koflox.location.geolocation

import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    suspend fun getCurrentLocation(): Result<Location>
    fun observeLocationUpdates(
        intervalMs: Long,
        inUpdateDistanceMeters: Float,
    ): Flow<Location>
}
