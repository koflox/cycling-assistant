package com.koflox.session.presentation.mapper

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.presentation.model.DisplayStat

internal interface SessionFormatMapper {
    fun formatElapsedTime(elapsedMs: Long): String
    fun formatDistance(distanceKm: Double): String
    fun formatSpeed(speedKmh: Double): String
    fun formatAltitudeGain(altitudeGainMeters: Double): String
    fun formatCalories(calories: Double): String
    fun formatPower(powerWatts: Int): String
    fun formatStartDate(startTimeMs: Long): String
}

internal interface SessionStatsMapper {
    fun toSessionUiModel(session: Session): SessionUiModel
    fun buildActiveSessionStats(
        session: Session,
        statTypes: List<SessionStatType>,
    ): List<DisplayStat>
    fun buildCompletedSessionStats(
        session: Session,
        derivedStats: SessionDerivedStats,
        statTypes: List<SessionStatType>,
    ): List<DisplayStat>
}

internal interface SessionUiMapper : SessionFormatMapper, SessionStatsMapper

internal data class SessionUiModel(
    val elapsedTimeFormatted: String,
    val traveledDistanceFormatted: String,
    val averageSpeedFormatted: String,
    val topSpeedFormatted: String,
    val altitudeGainFormatted: String,
)
