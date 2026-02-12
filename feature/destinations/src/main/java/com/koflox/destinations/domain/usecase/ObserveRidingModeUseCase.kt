package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import kotlinx.coroutines.flow.Flow

internal interface ObserveRidingModeUseCase {
    fun observe(): Flow<RidingMode>
}

internal class ObserveRidingModeUseCaseImpl(
    private val repository: RidePreferencesRepository,
) : ObserveRidingModeUseCase {
    override fun observe(): Flow<RidingMode> = repository.observeRidingMode()
}
