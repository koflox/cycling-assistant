package com.koflox.settings.data.repository

import com.koflox.settings.api.LocaleProvider
import com.koflox.settings.api.ThemeProvider
import com.koflox.settings.data.source.SettingsDataStore
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

internal class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore,
) : SettingsRepository, ThemeProvider, LocaleProvider {
    override fun observeTheme(): Flow<AppTheme> = dataStore.observeTheme()
    override fun observeLanguage(): Flow<AppLanguage> = dataStore.observeLanguage()

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.setTheme(theme)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.setLanguage(language)
    }
}
