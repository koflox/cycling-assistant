package com.koflox.session.domain.usecase

import com.koflox.altitude.AltitudeCalculator
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.location.validator.LocationValidator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UpdateSessionLocationUseCase {
    suspend fun update(location: Location, timestampMs: Long)
}

internal class UpdateSessionLocationUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionRepository: SessionRepository,
    private val distanceCalculator: DistanceCalculator,
    private val altitudeCalculator: AltitudeCalculator,
    private val locationValidator: LocationValidator,
) : UpdateSessionLocationUseCase {

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
        private const val MIN_DISTANCE_METERS = 3.0
        private const val MAX_SPEED_KMH = 100.0
        private const val METERS_PER_KM = 1000.0
    }

    override suspend fun update(location: Location, timestampMs: Long) = withContext(dispatcherDefault) {
        if (!locationValidator.isAccuracyValid(location)) return@withContext
        val session = activeSessionUseCase.getActiveSession()
        if (session.status != SessionStatus.RUNNING) return@withContext
        val previousTrackPoint = session.trackPoints.lastOrNull()
        val distanceKm = previousTrackPoint?.let {
            distanceCalculator.calculateKm(it.latitude, it.longitude, location.latitude, location.longitude)
        } ?: 0.0
        val timeDiffMs = previousTrackPoint?.let { timestampMs - it.timestampMs } ?: 0L
        val speedKmh = if (timeDiffMs > 0) (distanceKm / timeDiffMs) * MILLISECONDS_PER_HOUR else 0.0
        val isValid = previousTrackPoint == null || isLocationValid(distanceKm, speedKmh)
        if (isValid) {
            saveTrackPoint(session, location, timestampMs, distanceKm, speedKmh)
        }
    }

    private fun isLocationValid(distanceKm: Double, speedKmh: Double): Boolean =
        distanceKm * METERS_PER_KM >= MIN_DISTANCE_METERS && speedKmh <= MAX_SPEED_KMH

    private suspend fun saveTrackPoint(
        session: Session,
        location: Location,
        timestampMs: Long,
        distanceKm: Double,
        speedKmh: Double,
    ) {
        val previousTrackPoint = session.trackPoints.lastOrNull()
        val altitudeGain = altitudeCalculator.calculateGain(previousTrackPoint?.altitudeMeters, location.altitudeMeters)
        val newTrackPoint = TrackPoint(location.latitude, location.longitude, timestampMs, speedKmh, location.altitudeMeters)
        val totalDistanceKm = session.traveledDistanceKm + distanceKm
        val elapsedTimeMs = session.elapsedTimeMs + (timestampMs - session.lastResumedTimeMs)
        val updatedSession = session.copy(
            elapsedTimeMs = elapsedTimeMs,
            lastResumedTimeMs = timestampMs,
            traveledDistanceKm = totalDistanceKm,
            averageSpeedKmh = if (elapsedTimeMs > 0) (totalDistanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR else 0.0,
            topSpeedKmh = maxOf(session.topSpeedKmh, speedKmh),
            totalAltitudeGainMeters = session.totalAltitudeGainMeters + altitudeGain,
            trackPoints = session.trackPoints + newTrackPoint,
        )
        sessionRepository.saveSession(updatedSession)
    }
}
