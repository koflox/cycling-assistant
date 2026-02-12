package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.UserLocationRepository
import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

interface ObserveUserLocationUseCase {
    fun observe(): Flow<Location>
}

internal class ObserveUserLocationUseCaseImpl(
    private val repository: UserLocationRepository,
) : ObserveUserLocationUseCase {
    override fun observe(): Flow<Location> = repository.observeUserLocation()
}
