package com.koflox.settings.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)

internal class SettingsDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : SettingsLocalDataSource {
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_RIDER_WEIGHT_KG = doublePreferencesKey("rider_weight_kg")
        private const val DEFAULT_RIDER_WEIGHT_KG = 75f
    }

    override fun observeTheme(): Flow<AppTheme> = context.settingsDataStore.data
        .map { prefs ->
            prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
        }
        .flowOn(dispatcherIo)

    override fun observeLanguage(): Flow<AppLanguage> = context.settingsDataStore.data
        .map { prefs ->
            prefs[KEY_LANGUAGE]?.let { code ->
                AppLanguage.entries.find { it.code == code }
            } ?: AppLanguage.ENGLISH
        }
        .flowOn(dispatcherIo)

    override suspend fun getRiderWeightKg(): Float = withContext(dispatcherIo) {
        context.settingsDataStore.data.first()[KEY_RIDER_WEIGHT_KG]?.toFloat() ?: DEFAULT_RIDER_WEIGHT_KG
    }

    override suspend fun setTheme(theme: AppTheme) {
        withContext(dispatcherIo) {
            context.settingsDataStore.edit { prefs ->
                prefs[KEY_THEME] = theme.name
            }
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        withContext(dispatcherIo) {
            context.settingsDataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = language.code
            }
        }
    }

    override suspend fun setRiderWeightKg(weightKg: Double) {
        withContext(dispatcherIo) {
            context.settingsDataStore.edit { prefs ->
                prefs[KEY_RIDER_WEIGHT_KG] = weightKg
            }
        }
    }
}
