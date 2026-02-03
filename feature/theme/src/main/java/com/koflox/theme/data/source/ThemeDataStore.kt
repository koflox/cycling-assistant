package com.koflox.theme.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.theme.domain.model.AppTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_settings",
)

internal class ThemeDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : ThemeLocalDataSource {

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
    }

    override fun observeTheme(): Flow<AppTheme> = context.themeDataStore.data
        .map { prefs ->
            prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
        }
        .flowOn(dispatcherIo)

    override suspend fun setTheme(theme: AppTheme) {
        withContext(dispatcherIo) {
            context.themeDataStore.edit { prefs ->
                prefs[KEY_THEME] = theme.name
            }
        }
    }
}
