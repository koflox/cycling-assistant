package com.koflox.session.domain.usecase

import com.koflox.altitude.AltitudeCalculator
import com.koflox.distance.DistanceCalculator
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository

interface UpdateSessionLocationUseCase {
    suspend fun update(latitude: Double, longitude: Double, timestampMs: Long, altitudeMeters: Double?)
}

internal class UpdateSessionLocationUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionRepository: SessionRepository,
    private val distanceCalculator: DistanceCalculator,
    private val altitudeCalculator: AltitudeCalculator,
) : UpdateSessionLocationUseCase {

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
    }

    override suspend fun update(latitude: Double, longitude: Double, timestampMs: Long, altitudeMeters: Double?) {
        val session = activeSessionUseCase.getActiveSession()
        if (session.status != SessionStatus.RUNNING) {
            return
        }
        val previousTrackPoint = session.trackPoints.lastOrNull()
        val distanceKm = if (previousTrackPoint != null) {
            distanceCalculator.calculateKm(
                lat1 = previousTrackPoint.latitude,
                lon1 = previousTrackPoint.longitude,
                lat2 = latitude,
                lon2 = longitude,
            )
        } else {
            0.0
        }
        val timeDiffMs = if (previousTrackPoint != null) {
            timestampMs - previousTrackPoint.timestampMs
        } else {
            0L
        }
        val speedKmh = if (timeDiffMs > 0) {
            (distanceKm / timeDiffMs) * MILLISECONDS_PER_HOUR
        } else {
            0.0
        }
        val altitudeGain = altitudeCalculator.calculateGain(
            previousAltitude = previousTrackPoint?.altitudeMeters,
            currentAltitude = altitudeMeters,
        )
        val newTrackPoint = TrackPoint(
            latitude = latitude,
            longitude = longitude,
            timestampMs = timestampMs,
            speedKmh = speedKmh,
            altitudeMeters = altitudeMeters,
        )
        val updatedTrackPoints = session.trackPoints + newTrackPoint
        val totalDistanceKm = session.traveledDistanceKm + distanceKm
        val newTopSpeedKmh = maxOf(session.topSpeedKmh, speedKmh)
        val totalAltitudeGainMeters = session.totalAltitudeGainMeters + altitudeGain
        val elapsedSinceLastResume = timestampMs - session.lastResumedTimeMs
        val elapsedTimeMs = session.elapsedTimeMs + elapsedSinceLastResume
        val averageSpeedKmh = if (elapsedTimeMs > 0) {
            (totalDistanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR
        } else {
            0.0
        }
        val updatedSession = session.copy(
            elapsedTimeMs = elapsedTimeMs,
            lastResumedTimeMs = timestampMs,
            traveledDistanceKm = totalDistanceKm,
            averageSpeedKmh = averageSpeedKmh,
            topSpeedKmh = newTopSpeedKmh,
            totalAltitudeGainMeters = totalAltitudeGainMeters,
            trackPoints = updatedTrackPoints,
        )
        sessionRepository.saveSession(updatedSession)
    }
}
