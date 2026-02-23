package com.koflox.destinations.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val Context.destinationFilesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "destination_files",
)

internal class DestinationFilesLocalDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val context: Context,
    private val mutex: Mutex,
) : DestinationFilesLocalDataSource {

    companion object {
        private val KEY_LOADED_FILES = stringSetPreferencesKey("loaded_destination_files")
    }

    override suspend fun getLoadedFiles(): Set<String> = withContext(dispatcherIo) {
        mutex.withLock {
            context.destinationFilesDataStore.data.first()[KEY_LOADED_FILES] ?: emptySet()
        }
    }

    override suspend fun addLoadedFile(fileName: String) = withContext(dispatcherIo) {
        mutex.withLock {
            context.destinationFilesDataStore.edit { prefs ->
                val currentFiles = prefs[KEY_LOADED_FILES] ?: emptySet()
                prefs[KEY_LOADED_FILES] = currentFiles + fileName
            }
        }
        Unit
    }

    override suspend fun clearLoadedFiles() = withContext(dispatcherIo) {
        mutex.withLock {
            context.destinationFilesDataStore.edit { prefs -> prefs.remove(KEY_LOADED_FILES) }
        }
        Unit
    }
}
