package com.koflox.session.data.source.local

import com.koflox.session.domain.model.SessionStatType
import kotlinx.coroutines.flow.Flow

internal interface StatsDisplayLocalDataSource {
    fun observeActiveSessionStats(): Flow<List<SessionStatType>>
    fun observeCompletedSessionStats(): Flow<List<SessionStatType>>
    fun observeShareStats(): Flow<List<SessionStatType>>
    suspend fun updateActiveSessionStats(stats: List<SessionStatType>)
    suspend fun updateCompletedSessionStats(stats: List<SessionStatType>)
    suspend fun updateShareStats(stats: List<SessionStatType>)
}
