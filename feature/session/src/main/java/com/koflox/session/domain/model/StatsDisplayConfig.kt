package com.koflox.session.domain.model

data class StatsDisplayConfig(
    val activeSessionStats: List<SessionStatType>,
    val completedSessionStats: List<SessionStatType>,
    val shareStats: List<SessionStatType>,
) {

    companion object {
        const val ACTIVE_SESSION_STATS_COUNT = 5
        const val COMPLETED_SESSION_MIN_STATS = 4
        const val SHARE_MIN_STATS = 4
        const val SHARE_MAX_STATS = 8

        val ACTIVE_SESSION_POOL = listOf(
            SessionStatType.TIME,
            SessionStatType.DISTANCE,
            SessionStatType.AVG_SPEED,
            SessionStatType.TOP_SPEED,
            SessionStatType.ALTITUDE_GAIN,
            SessionStatType.AVG_POWER,
            SessionStatType.MAX_POWER,
            SessionStatType.CALORIES_POWER,
        )

        val COMPLETED_SHARE_POOL = SessionStatType.entries.toList()

        val DEFAULT_ACTIVE_SESSION_STATS = listOf(
            SessionStatType.TIME,
            SessionStatType.DISTANCE,
            SessionStatType.AVG_SPEED,
            SessionStatType.TOP_SPEED,
            SessionStatType.ALTITUDE_GAIN,
        )

        val DEFAULT_COMPLETED_SESSION_STATS = listOf(
            SessionStatType.TIME,
            SessionStatType.DISTANCE,
            SessionStatType.MOVING_TIME,
            SessionStatType.IDLE_TIME,
            SessionStatType.AVG_SPEED,
            SessionStatType.TOP_SPEED,
            SessionStatType.ALTITUDE_GAIN,
            SessionStatType.ALTITUDE_LOSS,
        )

        val DEFAULT_SHARE_STATS = listOf(
            SessionStatType.TIME,
            SessionStatType.DISTANCE,
            SessionStatType.TOP_SPEED,
            SessionStatType.ALTITUDE_GAIN,
        )

        val DEFAULT = StatsDisplayConfig(
            activeSessionStats = DEFAULT_ACTIVE_SESSION_STATS,
            completedSessionStats = DEFAULT_COMPLETED_SESSION_STATS,
            shareStats = DEFAULT_SHARE_STATS,
        )
    }
}
