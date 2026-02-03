package com.koflox.nutrition.data.source

import com.koflox.nutrition.domain.model.NutritionSettings
import kotlinx.coroutines.flow.Flow

internal interface NutritionSettingsLocalDataSource {
    fun observeSettings(): Flow<NutritionSettings>
    suspend fun setEnabled(enabled: Boolean)
    suspend fun setIntervalMinutes(intervalMinutes: Int)
}
