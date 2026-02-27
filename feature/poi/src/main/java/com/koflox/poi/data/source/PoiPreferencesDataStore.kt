package com.koflox.poi.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.poi.domain.model.PoiType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.poiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "poi_settings",
)

internal class PoiPreferencesDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : PoiLocalDataSource {

    companion object {
        private val KEY_SELECTED_POIS = stringSetPreferencesKey("selected_pois")
        private val DEFAULT_POIS = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
    }

    override fun observeSelectedPois(): Flow<List<PoiType>> = context.poiDataStore.data
        .map { prefs ->
            val names = prefs[KEY_SELECTED_POIS]
            if (names.isNullOrEmpty()) {
                DEFAULT_POIS
            } else {
                val validNames = PoiType.entries.associateBy { it.name }
                names.mapNotNull { name -> validNames[name] }
            }
        }
        .flowOn(dispatcherIo)

    override suspend fun updateSelectedPois(pois: List<PoiType>) {
        withContext(dispatcherIo) {
            context.poiDataStore.edit { prefs ->
                prefs[KEY_SELECTED_POIS] = pois.map { it.name }.toSet()
            }
        }
    }
}
