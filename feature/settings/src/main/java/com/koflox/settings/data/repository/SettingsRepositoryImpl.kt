package com.koflox.settings.data.repository

import com.koflox.settings.api.LocaleProvider
import com.koflox.settings.api.ThemeProvider
import com.koflox.settings.data.source.SettingsLocalDataSource
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

internal class SettingsRepositoryImpl(
    private val localDataSource: SettingsLocalDataSource,
) : SettingsRepository, ThemeProvider, LocaleProvider {
    override fun observeTheme(): Flow<AppTheme> = localDataSource.observeTheme()
    override fun observeLanguage(): Flow<AppLanguage> = localDataSource.observeLanguage()

    override suspend fun setTheme(theme: AppTheme) {
        localDataSource.setTheme(theme)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        localDataSource.setLanguage(language)
    }
}
