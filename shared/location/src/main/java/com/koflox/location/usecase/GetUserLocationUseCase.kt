package com.koflox.location.usecase

import com.koflox.location.model.Location
import com.koflox.location.repository.UserLocationRepository

interface GetUserLocationUseCase {
    suspend fun getLocation(): Result<Location>
}

internal class GetUserLocationUseCaseImpl(
    private val repository: UserLocationRepository,
) : GetUserLocationUseCase {
    override suspend fun getLocation(): Result<Location> = repository.getUserLocation()
}
