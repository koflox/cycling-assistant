package com.koflox.session.data.repository

import com.koflox.session.data.source.local.StatsDisplayLocalDataSource
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.repository.StatsDisplayRepository
import kotlinx.coroutines.flow.Flow

internal class StatsDisplayRepositoryImpl(
    private val localDataSource: StatsDisplayLocalDataSource,
) : StatsDisplayRepository {

    override fun observeActiveSessionStats(): Flow<List<SessionStatType>> =
        localDataSource.observeActiveSessionStats()

    override fun observeCompletedSessionStats(): Flow<List<SessionStatType>> =
        localDataSource.observeCompletedSessionStats()

    override fun observeShareStats(): Flow<List<SessionStatType>> =
        localDataSource.observeShareStats()

    override suspend fun updateActiveSessionStats(stats: List<SessionStatType>) {
        localDataSource.updateActiveSessionStats(stats)
    }

    override suspend fun updateCompletedSessionStats(stats: List<SessionStatType>) {
        localDataSource.updateCompletedSessionStats(stats)
    }

    override suspend fun updateShareStats(stats: List<SessionStatType>) {
        localDataSource.updateShareStats(stats)
    }
}
