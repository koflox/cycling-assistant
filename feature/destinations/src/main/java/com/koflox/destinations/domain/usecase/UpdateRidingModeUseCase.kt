package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidePreferencesRepository

internal interface UpdateRidingModeUseCase {
    suspend fun update(mode: RidingMode)
}

internal class UpdateRidingModeUseCaseImpl(
    private val repository: RidePreferencesRepository,
) : UpdateRidingModeUseCase {
    override suspend fun update(mode: RidingMode) = repository.setRidingMode(mode)
}
