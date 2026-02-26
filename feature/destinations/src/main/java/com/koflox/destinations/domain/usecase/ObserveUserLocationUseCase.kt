package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.UserLocationRepository
import com.koflox.location.model.Location
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
