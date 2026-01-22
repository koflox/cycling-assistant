package com.koflox.settings.domain.repository

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

internal interface SettingsRepository {
    fun observeTheme(): Flow<AppTheme>
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
}
