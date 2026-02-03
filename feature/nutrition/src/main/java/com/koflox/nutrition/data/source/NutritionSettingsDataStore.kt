package com.koflox.nutrition.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.nutrition.domain.model.NutritionSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.nutritionSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nutrition_settings",
)

internal class NutritionSettingsDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : NutritionSettingsLocalDataSource {

    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("nutrition_enabled")
        private val KEY_INTERVAL_MINUTES = intPreferencesKey("nutrition_interval_minutes")
    }

    override fun observeSettings(): Flow<NutritionSettings> = context.nutritionSettingsDataStore.data
        .map { prefs ->
            @Suppress("KotlinConstantConditions")
            NutritionSettings(
                isEnabled = prefs[KEY_ENABLED] ?: NutritionSettings.DEFAULT_ENABLED,
                intervalMinutes = prefs[KEY_INTERVAL_MINUTES] ?: NutritionSettings.DEFAULT_INTERVAL_MINUTES,
            )
        }
        .flowOn(dispatcherIo)

    override suspend fun setEnabled(enabled: Boolean) {
        withContext(dispatcherIo) {
            context.nutritionSettingsDataStore.edit { prefs ->
                prefs[KEY_ENABLED] = enabled
            }
        }
    }

    override suspend fun setIntervalMinutes(intervalMinutes: Int) {
        withContext(dispatcherIo) {
            context.nutritionSettingsDataStore.edit { prefs ->
                prefs[KEY_INTERVAL_MINUTES] = intervalMinutes
            }
        }
    }
}
