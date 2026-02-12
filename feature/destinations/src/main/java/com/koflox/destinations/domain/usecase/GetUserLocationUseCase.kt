package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.UserLocationRepository
import com.koflox.location.model.Location

interface GetUserLocationUseCase {
    suspend fun getLocation(): Result<Location>
}

internal class GetUserLocationUseCaseImpl(
    private val repository: UserLocationRepository,
) : GetUserLocationUseCase {
    override suspend fun getLocation(): Result<Location> = repository.getUserLocation()
}
