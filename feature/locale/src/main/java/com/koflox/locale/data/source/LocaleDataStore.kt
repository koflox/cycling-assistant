package com.koflox.locale.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.locale.domain.model.AppLanguage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.localeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "locale_settings",
)

internal class LocaleDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : LocaleLocalDataSource {

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    override fun observeLanguage(): Flow<AppLanguage> = context.localeDataStore.data
        .map { prefs ->
            prefs[KEY_LANGUAGE]?.let { code ->
                AppLanguage.entries.find { it.code == code }
            } ?: AppLanguage.ENGLISH
        }
        .flowOn(dispatcherIo)

    override suspend fun setLanguage(language: AppLanguage) {
        withContext(dispatcherIo) {
            context.localeDataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = language.code
            }
        }
    }
}
