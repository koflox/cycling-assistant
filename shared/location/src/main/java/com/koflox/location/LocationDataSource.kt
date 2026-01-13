package com.koflox.location

import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    companion object {
        const val DEFAULT_LOCATION_INTERVAL_MS = 3000L
        const val DEFAULT_MIN_UPDATE_DISTANCE_METERS = 50F
    }

    suspend fun getCurrentLocation(): Result<Location>
    fun observeLocationUpdates(
        intervalMs: Long = DEFAULT_LOCATION_INTERVAL_MS,
        inUpdateDistanceMeters: Float = DEFAULT_MIN_UPDATE_DISTANCE_METERS,
    ): Flow<Location>

}
