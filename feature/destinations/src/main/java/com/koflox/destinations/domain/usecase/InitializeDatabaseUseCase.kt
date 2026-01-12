package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.DestinationRepository

interface InitializeDatabaseUseCase {
    suspend fun init(): Result<Unit>
}

internal class InitializeDatabaseUseCaseImpl(
    private val repository: DestinationRepository,
) : InitializeDatabaseUseCase {
    override suspend fun init(): Result<Unit> = repository.initializeDatabase()
}
