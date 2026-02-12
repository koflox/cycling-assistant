package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.repository.DestinationsRepository
import com.koflox.location.model.Location
import kotlinx.coroutines.flow.Flow

internal interface InitializeDatabaseUseCase {
    fun init(location: Location): Flow<DestinationLoadingEvent>
}

internal class InitializeDatabaseUseCaseImpl(
    private val repository: DestinationsRepository,
) : InitializeDatabaseUseCase {
    override fun init(location: Location): Flow<DestinationLoadingEvent> =
        repository.loadDestinationsForLocation(location)
}
