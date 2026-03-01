package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.repository.StatsDisplayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface ObserveStatsDisplayConfigUseCase {
    fun observeActiveSessionStats(): Flow<List<SessionStatType>>
    fun observeCompletedSessionStats(): Flow<List<SessionStatType>>
    fun observeShareStats(): Flow<List<SessionStatType>>
    fun observeStatsDisplayConfig(): Flow<StatsDisplayConfig>
}

internal class ObserveStatsDisplayConfigUseCaseImpl(
    private val repository: StatsDisplayRepository,
) : ObserveStatsDisplayConfigUseCase {

    override fun observeActiveSessionStats(): Flow<List<SessionStatType>> =
        repository.observeActiveSessionStats()

    override fun observeCompletedSessionStats(): Flow<List<SessionStatType>> =
        repository.observeCompletedSessionStats()

    override fun observeShareStats(): Flow<List<SessionStatType>> =
        repository.observeShareStats()

    override fun observeStatsDisplayConfig(): Flow<StatsDisplayConfig> = combine(
        repository.observeActiveSessionStats(),
        repository.observeCompletedSessionStats(),
        repository.observeShareStats(),
    ) { active, completed, share ->
        StatsDisplayConfig(
            activeSessionStats = active,
            completedSessionStats = completed,
            shareStats = share,
        )
    }
}
