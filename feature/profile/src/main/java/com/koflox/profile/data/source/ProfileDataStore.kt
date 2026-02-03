package com.koflox.profile.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "profile_settings",
)

internal class ProfileDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : ProfileLocalDataSource {

    companion object {
        private val KEY_RIDER_WEIGHT_KG = doublePreferencesKey("rider_weight_kg")
    }

    override suspend fun getRiderWeightKg(): Float? = withContext(dispatcherIo) {
        context.profileDataStore
            .data
            .map { prefs -> prefs[KEY_RIDER_WEIGHT_KG]?.toFloat() }
            .firstOrNull()
    }

    override suspend fun setRiderWeightKg(weightKg: Double) {
        withContext(dispatcherIo) {
            context.profileDataStore.edit { prefs ->
                prefs[KEY_RIDER_WEIGHT_KG] = weightKg
            }
        }
    }
}
