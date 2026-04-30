package com.koflox.session.domain.repository

import com.koflox.session.domain.model.SessionStatType
import kotlinx.coroutines.flow.Flow

interface StatsDisplayRepository {
    fun observeActiveSessionStats(): Flow<List<SessionStatType>>
    fun observeCompletedSessionStats(): Flow<List<SessionStatType>>
    fun observeShareStats(): Flow<List<SessionStatType>>
    suspend fun updateActiveSessionStats(stats: List<SessionStatType>)
    suspend fun updateCompletedSessionStats(stats: List<SessionStatType>)
    suspend fun updateShareStats(stats: List<SessionStatType>)
}
