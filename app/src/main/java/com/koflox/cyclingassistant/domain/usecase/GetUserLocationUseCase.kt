package com.koflox.cyclingassistant.domain.usecase

import com.koflox.cyclingassistant.domain.model.Location
import com.koflox.cyclingassistant.domain.repository.DestinationRepository

interface GetUserLocationUseCase {
    suspend fun getLocation(): Result<Location>
}

internal class GetUserLocationUseCaseImpl(
    private val repository: DestinationRepository,
) : GetUserLocationUseCase {
    override suspend fun getLocation(): Result<Location> = repository.getUserLocation()
}
