package com.koflox.location.usecase

import com.koflox.location.model.Location
import com.koflox.location.repository.UserLocationRepository
import kotlinx.coroutines.flow.Flow

interface ObserveUserLocationUseCase {
    fun observe(intervalMs: Long, minUpdateDistanceMeters: Float): Flow<Location>
}

internal class ObserveUserLocationUseCaseImpl(
    private val repository: UserLocationRepository,
) : ObserveUserLocationUseCase {
    override fun observe(intervalMs: Long, minUpdateDistanceMeters: Float): Flow<Location> =
        repository.observeUserLocation(intervalMs, minUpdateDistanceMeters)
}
