package com.koflox.destinations.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.destinations.domain.model.RidingMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val RIDING_MODE_STORE_NAME = "riding_mode"
private val Context.ridingModeDataStore by preferencesDataStore(name = RIDING_MODE_STORE_NAME)

internal class RidingModeLocalDataSourceImpl(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : RidingModeLocalDataSource {

    companion object {
        private val KEY_RIDING_MODE = stringPreferencesKey(RIDING_MODE_STORE_NAME)
    }

    override fun observeRidingMode(): Flow<RidingMode> = context.ridingModeDataStore.data
        .map { prefs ->
            prefs[KEY_RIDING_MODE]?.let { runCatching { RidingMode.valueOf(it) }.getOrNull() }
                ?: RidingMode.FREE_ROAM
        }
        .flowOn(dispatcherIo)

    override suspend fun setRidingMode(mode: RidingMode) {
        withContext(dispatcherIo) {
            context.ridingModeDataStore.edit { prefs ->
                prefs[KEY_RIDING_MODE] = mode.name
            }
        }
    }
}
