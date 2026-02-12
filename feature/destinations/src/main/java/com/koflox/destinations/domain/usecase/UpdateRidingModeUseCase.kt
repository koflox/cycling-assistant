package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidingModeRepository

internal interface UpdateRidingModeUseCase {
    suspend fun update(mode: RidingMode)
}

internal class UpdateRidingModeUseCaseImpl(
    private val repository: RidingModeRepository,
) : UpdateRidingModeUseCase {
    override suspend fun update(mode: RidingMode) = repository.setRidingMode(mode)
}
