package com.koflox.nutrition.domain.usecase

import com.koflox.nutrition.domain.repository.NutritionSettingsRepository

interface UpdateNutritionSettingsUseCase {
    suspend fun setEnabled(enabled: Boolean)
    suspend fun setIntervalMinutes(intervalMinutes: Int)
}

internal class UpdateNutritionSettingsUseCaseImpl(
    private val repository: NutritionSettingsRepository,
) : UpdateNutritionSettingsUseCase {

    override suspend fun setEnabled(enabled: Boolean) {
        repository.setEnabled(enabled)
    }

    override suspend fun setIntervalMinutes(intervalMinutes: Int) {
        repository.setIntervalMinutes(intervalMinutes)
    }
}
