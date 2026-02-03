package com.koflox.nutrition.domain.usecase

import com.koflox.nutrition.domain.model.NutritionSettings
import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import kotlinx.coroutines.flow.Flow

interface ObserveNutritionSettingsUseCase {
    fun observeSettings(): Flow<NutritionSettings>
}

internal class ObserveNutritionSettingsUseCaseImpl(
    private val repository: NutritionSettingsRepository,
) : ObserveNutritionSettingsUseCase {
    override fun observeSettings(): Flow<NutritionSettings> = repository.observeSettings()
}
