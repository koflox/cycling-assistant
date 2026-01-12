package com.koflox.cyclingassistant.domain.usecase

import com.koflox.cyclingassistant.domain.repository.DestinationRepository

// TODO: Combine to another use case which will be invoked on user location updates to gradually parse locations related to current area only
interface InitializeDatabaseUseCase {
    suspend fun init(): Result<Unit>
}

internal class InitializeDatabaseUseCaseImpl(
    private val repository: DestinationRepository,
) : InitializeDatabaseUseCase {
    override suspend fun init(): Result<Unit> = repository.initializeDatabase()
}
