package com.koflox.destinations.data.source.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PreferencesDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val prefs: SharedPreferences,
) : PreferencesDataSource {

    companion object {
        private const val KEY_LOADED_FILES = "loaded_destination_files"
    }

    override suspend fun getLoadedFiles(): Set<String> = withContext(dispatcherIo) {
        prefs.getStringSet(KEY_LOADED_FILES, emptySet()) ?: emptySet()
    }

    override suspend fun addLoadedFile(fileName: String) = withContext(dispatcherIo) {
        val currentFiles = prefs.getStringSet(KEY_LOADED_FILES, emptySet()) ?: emptySet()
        prefs.edit(commit = true) {
            putStringSet(KEY_LOADED_FILES, currentFiles + fileName)
        }
    }
}
