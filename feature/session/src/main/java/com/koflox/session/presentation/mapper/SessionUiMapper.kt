package com.koflox.session.presentation.mapper

import com.koflox.session.domain.model.Session

interface SessionUiMapper {
    fun formatElapsedTime(elapsedMs: Long): String
    fun formatDistance(distanceKm: Double): String
    fun formatSpeed(speedKmh: Double): String
    fun formatStartDate(startTimeMs: Long): String
    fun toSessionUiModel(session: Session): SessionUiModel
}

data class SessionUiModel(
    val elapsedTimeFormatted: String,
    val traveledDistanceFormatted: String,
    val averageSpeedFormatted: String,
    val topSpeedFormatted: String,
)
