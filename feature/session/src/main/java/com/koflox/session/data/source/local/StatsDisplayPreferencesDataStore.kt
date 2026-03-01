package com.koflox.session.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.model.StatsDisplayConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.statsDisplayDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stats_display_settings",
)

internal class StatsDisplayPreferencesDataStore(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : StatsDisplayLocalDataSource {

    companion object {
        private const val DELIMITER = ","
        private val KEY_ACTIVE_SESSION_STATS = stringPreferencesKey("active_session_stats")
        private val KEY_COMPLETED_SESSION_STATS = stringPreferencesKey("completed_session_stats")
        private val KEY_SHARE_STATS = stringPreferencesKey("share_stats")
    }

    override fun observeActiveSessionStats(): Flow<List<SessionStatType>> =
        observeStats(KEY_ACTIVE_SESSION_STATS, StatsDisplayConfig.DEFAULT_ACTIVE_SESSION_STATS)

    override fun observeCompletedSessionStats(): Flow<List<SessionStatType>> =
        observeStats(KEY_COMPLETED_SESSION_STATS, StatsDisplayConfig.DEFAULT_COMPLETED_SESSION_STATS)

    override fun observeShareStats(): Flow<List<SessionStatType>> =
        observeStats(KEY_SHARE_STATS, StatsDisplayConfig.DEFAULT_SHARE_STATS)

    override suspend fun updateActiveSessionStats(stats: List<SessionStatType>) {
        updateStats(KEY_ACTIVE_SESSION_STATS, stats)
    }

    override suspend fun updateCompletedSessionStats(stats: List<SessionStatType>) {
        updateStats(KEY_COMPLETED_SESSION_STATS, stats)
    }

    override suspend fun updateShareStats(stats: List<SessionStatType>) {
        updateStats(KEY_SHARE_STATS, stats)
    }

    private fun observeStats(
        key: Preferences.Key<String>,
        defaults: List<SessionStatType>,
    ): Flow<List<SessionStatType>> = context.statsDisplayDataStore.data
        .map { prefs ->
            val csv = prefs[key]
            if (csv.isNullOrEmpty()) {
                defaults
            } else {
                val validNames = SessionStatType.entries.associateBy { it.name }
                csv.split(DELIMITER).mapNotNull { name -> validNames[name] }
            }
        }
        .flowOn(dispatcherIo)

    private suspend fun updateStats(key: Preferences.Key<String>, stats: List<SessionStatType>) {
        withContext(dispatcherIo) {
            context.statsDisplayDataStore.edit { prefs ->
                prefs[key] = stats.joinToString(DELIMITER) { it.name }
            }
        }
    }
}
