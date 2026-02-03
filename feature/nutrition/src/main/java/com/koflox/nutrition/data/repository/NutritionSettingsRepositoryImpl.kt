package com.koflox.nutrition.data.repository

import com.koflox.nutrition.data.source.NutritionSettingsLocalDataSource
import com.koflox.nutrition.domain.model.NutritionSettings
import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import kotlinx.coroutines.flow.Flow

internal class NutritionSettingsRepositoryImpl(
    private val localDataSource: NutritionSettingsLocalDataSource,
) : NutritionSettingsRepository {

    override fun observeSettings(): Flow<NutritionSettings> = localDataSource.observeSettings()

    override suspend fun setEnabled(enabled: Boolean) {
        localDataSource.setEnabled(enabled)
    }

    override suspend fun setIntervalMinutes(intervalMinutes: Int) {
        localDataSource.setIntervalMinutes(intervalMinutes)
    }
}
