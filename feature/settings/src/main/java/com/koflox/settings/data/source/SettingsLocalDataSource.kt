package com.koflox.settings.data.source

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

internal interface SettingsLocalDataSource {
    fun observeTheme(): Flow<AppTheme>
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun getRiderWeightKg(): Float
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setRiderWeightKg(weightKg: Double)
}
