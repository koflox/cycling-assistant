package com.koflox.destinations.data.source.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PreferencesDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val prefs: SharedPreferences,
) : PreferencesDataSource {

    override suspend fun isDatabaseInitialized(): Boolean = withContext(dispatcherIo) {
        prefs.getBoolean(KEY_DB_INITIALIZED, false)
    }

    override suspend fun setDatabaseInitialized(initialized: Boolean) = withContext(dispatcherIo) {
        prefs.edit(commit = true) {
            putBoolean(KEY_DB_INITIALIZED, initialized)
        }
    }

    companion object {
        private const val KEY_DB_INITIALIZED = "database_initialized"
    }
}
