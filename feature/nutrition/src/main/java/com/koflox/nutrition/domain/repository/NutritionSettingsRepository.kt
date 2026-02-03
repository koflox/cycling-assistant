package com.koflox.nutrition.domain.repository

import com.koflox.nutrition.domain.model.NutritionSettings
import kotlinx.coroutines.flow.Flow

internal interface NutritionSettingsRepository {
    fun observeSettings(): Flow<NutritionSettings>
    suspend fun setEnabled(enabled: Boolean)
    suspend fun setIntervalMinutes(intervalMinutes: Int)
}
