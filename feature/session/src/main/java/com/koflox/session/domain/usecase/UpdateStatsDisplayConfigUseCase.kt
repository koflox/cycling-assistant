package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.repository.StatsDisplayRepository

internal interface UpdateStatsDisplayConfigUseCase {
    suspend fun updateActiveSessionStats(stats: List<SessionStatType>)
    suspend fun updateCompletedSessionStats(stats: List<SessionStatType>)
    suspend fun updateShareStats(stats: List<SessionStatType>)
}

internal class InvalidStatsSelectionException(message: String) : IllegalArgumentException(message)

internal class UpdateStatsDisplayConfigUseCaseImpl(
    private val repository: StatsDisplayRepository,
) : UpdateStatsDisplayConfigUseCase {

    override suspend fun updateActiveSessionStats(stats: List<SessionStatType>) {
        if (stats.size != StatsDisplayConfig.ACTIVE_SESSION_STATS_COUNT) {
            throw InvalidStatsSelectionException(
                "Active session requires exactly ${StatsDisplayConfig.ACTIVE_SESSION_STATS_COUNT} stats, got ${stats.size}",
            )
        }
        repository.updateActiveSessionStats(stats)
    }

    override suspend fun updateCompletedSessionStats(stats: List<SessionStatType>) {
        if (stats.size < StatsDisplayConfig.COMPLETED_SESSION_MIN_STATS) {
            throw InvalidStatsSelectionException(
                "Completed session requires at least ${StatsDisplayConfig.COMPLETED_SESSION_MIN_STATS} stats, got ${stats.size}",
            )
        }
        repository.updateCompletedSessionStats(stats)
    }

    override suspend fun updateShareStats(stats: List<SessionStatType>) {
        if (stats.size < StatsDisplayConfig.SHARE_MIN_STATS || stats.size > StatsDisplayConfig.SHARE_MAX_STATS) {
            throw InvalidStatsSelectionException(
                "Share requires ${StatsDisplayConfig.SHARE_MIN_STATS}-${StatsDisplayConfig.SHARE_MAX_STATS} stats, got ${stats.size}",
            )
        }
        repository.updateShareStats(stats)
    }
}
