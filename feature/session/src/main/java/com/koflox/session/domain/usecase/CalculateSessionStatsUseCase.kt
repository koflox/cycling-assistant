package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.TrackPoint
import com.koflox.sessionsettings.bridge.api.RiderProfileUseCase

interface CalculateSessionStatsUseCase {
    suspend fun calculate(sessionId: String): Result<SessionDerivedStats>
}

internal class CalculateSessionStatsUseCaseImpl(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val riderProfileUseCase: RiderProfileUseCase,
) : CalculateSessionStatsUseCase {

    companion object {
        private const val IDLE_SPEED_THRESHOLD_KMH = 2.0
        private const val MIN_ALTITUDE_LOSS_THRESHOLD_METERS = 1.0
        private const val MS_PER_HOUR = 3_600_000.0
    }

    /**
     * Computes derived session statistics: moving/idle time, altitude loss, and calories burned.
     *
     * Moving time is the sum of time gaps between consecutive track points where speed is
     * above the idle threshold; all remaining time is treated as idle.
     *
     * Calories are estimated using MET (Metabolic Equivalent of Task) — a ratio of the metabolic
     * rate during activity to the resting metabolic rate. The MET value is selected based on
     * average cycling speed and multiplied by rider weight and moving time.
     */
    override suspend fun calculate(sessionId: String): Result<SessionDerivedStats> {
        return getSessionByIdUseCase.getSession(sessionId).map { session ->
            val riderWeightKg = riderProfileUseCase.getRiderWeightKg()?.toDouble()
            val movingTimeMs = calculateMovingTimeMs(session.trackPoints, session.elapsedTimeMs)
            val idleTimeMs = (session.elapsedTimeMs - movingTimeMs).coerceAtLeast(0L)
            val altitudeLossMeters = calculateAltitudeLoss(session.trackPoints)
            val movingTimeHours = movingTimeMs / MS_PER_HOUR
            val caloriesBurned = riderWeightKg?.let {
                calculateCalories(session.averageSpeedKmh, it, movingTimeHours)
            }
            SessionDerivedStats(
                idleTimeMs = idleTimeMs,
                movingTimeMs = movingTimeMs,
                altitudeLossMeters = altitudeLossMeters,
                caloriesBurned = caloriesBurned,
            )
        }
    }

    private fun calculateMovingTimeMs(trackPoints: List<TrackPoint>, elapsedTimeMs: Long): Long {
        if (trackPoints.size < 2) return 0L
        var movingMs = 0L
        for (i in 1 until trackPoints.size) {
            if (trackPoints[i].speedKmh >= IDLE_SPEED_THRESHOLD_KMH) {
                movingMs += trackPoints[i].timestampMs - trackPoints[i - 1].timestampMs
            }
        }
        return movingMs.coerceAtMost(elapsedTimeMs)
    }

    private fun calculateAltitudeLoss(trackPoints: List<TrackPoint>): Double {
        if (trackPoints.size < 2) return 0.0
        return trackPoints.zipWithNext().sumOf { (prev, curr) ->
            val prevAlt = prev.altitudeMeters ?: return@sumOf 0.0
            val currAlt = curr.altitudeMeters ?: return@sumOf 0.0
            val diff = prevAlt - currAlt
            if (diff > MIN_ALTITUDE_LOSS_THRESHOLD_METERS) diff else 0.0
        }
    }

    private fun calculateCalories(
        averageSpeedKmh: Double,
        riderWeightKg: Double,
        movingTimeHours: Double,
    ): Double {
        val met = getMetForSpeed(averageSpeedKmh)
        return met * riderWeightKg * movingTimeHours
    }

    private fun getMetForSpeed(speedKmh: Double): Double = when {
        speedKmh < 16.0 -> 4.0
        speedKmh < 19.0 -> 6.8
        speedKmh < 22.0 -> 8.0
        speedKmh < 26.0 -> 10.0
        else -> 12.0
    }
}
