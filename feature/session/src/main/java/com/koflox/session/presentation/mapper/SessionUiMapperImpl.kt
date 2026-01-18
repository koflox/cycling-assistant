package com.koflox.session.presentation.mapper

import com.koflox.session.domain.model.Session
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionUiMapperImpl : SessionUiMapper {

    companion object {
        private const val DATE_FORMAT_PATTERN = "MMM dd, yyyy HH:mm"
    }

    override fun formatElapsedTime(elapsedMs: Long): String {
        val totalSeconds = elapsedMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun formatDistance(distanceKm: Double): String =
        String.format(Locale.getDefault(), "%.2f", distanceKm)

    override fun formatSpeed(speedKmh: Double): String =
        String.format(Locale.getDefault(), "%.1f", speedKmh)

    override fun formatStartDate(startTimeMs: Long): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
        return dateFormat.format(Date(startTimeMs))
    }

    override fun toSessionUiModel(session: Session): SessionUiModel = SessionUiModel(
        elapsedTimeFormatted = formatElapsedTime(session.elapsedTimeMs),
        traveledDistanceFormatted = formatDistance(session.traveledDistanceKm),
        averageSpeedFormatted = formatSpeed(session.averageSpeedKmh),
        topSpeedFormatted = formatSpeed(session.topSpeedKmh),
    )

}
