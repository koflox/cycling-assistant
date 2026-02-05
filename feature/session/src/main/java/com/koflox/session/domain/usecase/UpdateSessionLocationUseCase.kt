package com.koflox.session.domain.usecase

import com.koflox.altitude.AltitudeCalculator
import com.koflox.distance.DistanceCalculator
import com.koflox.id.IdGenerator
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
    private val idGenerator: IdGenerator,
) : UpdateSessionLocationUseCase {

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
        private const val MIN_DISTANCE_METERS = 5.0
        private const val MAX_SPEED_KMH = 100.0
        private const val METERS_PER_KM = 1000.0
    }

    override suspend fun update(location: Location, timestampMs: Long) = withContext(dispatcherDefault) {
        if (!locationValidator.isAccuracyValid(location)) return@withContext
        val session = activeSessionUseCase.getActiveSession()
        if (session.status != SessionStatus.RUNNING) return@withContext
        val previousTrackPoint = session.trackPoints.lastOrNull()
        if (previousTrackPoint == null || previousTrackPoint.timestampMs < session.lastResumedTimeMs) {
            saveSegmentStartPoint(session, location, timestampMs)
        } else {
            processNormalPoint(session, previousTrackPoint, location, timestampMs)
        }
    }

    private suspend fun saveSegmentStartPoint(
        session: Session,
        location: Location,
        timestampMs: Long,
    ) {
        val newTrackPoint = TrackPoint(
            id = idGenerator.generate(),
            latitude = location.latitude,
            longitude = location.longitude,
            timestampMs = timestampMs,
            speedKmh = 0.0,
            altitudeMeters = location.altitudeMeters,
            isSegmentStart = true,
            accuracyMeters = location.accuracyMeters,
        )
        val elapsedTimeMs = session.elapsedTimeMs + (timestampMs - session.lastResumedTimeMs)
        val totalDistanceKm = session.traveledDistanceKm
        val updatedSession = session.copy(
            elapsedTimeMs = elapsedTimeMs,
            lastResumedTimeMs = timestampMs,
            traveledDistanceKm = totalDistanceKm,
            averageSpeedKmh = if (elapsedTimeMs > 0) (totalDistanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR else 0.0,
            trackPoints = session.trackPoints + newTrackPoint,
        )
        sessionRepository.saveSession(updatedSession)
    }

    private suspend fun processNormalPoint(
        session: Session,
        previousTrackPoint: TrackPoint,
        location: Location,
        timestampMs: Long,
    ) {
        val distanceKm = distanceCalculator.calculateKm(
            previousTrackPoint.latitude, previousTrackPoint.longitude, location.latitude, location.longitude,
        )
        val displacementMeters = distanceKm * METERS_PER_KM
        val accuracy = location.accuracyMeters
        if (accuracy != null && displacementMeters < accuracy) return
        val timeDiffMs = timestampMs - previousTrackPoint.timestampMs
        val speedKmh = if (timeDiffMs > 0) (distanceKm / timeDiffMs) * MILLISECONDS_PER_HOUR else 0.0
        if (!isLocationValid(distanceKm, speedKmh)) return
        saveTrackPoint(session, location, timestampMs, distanceKm, speedKmh)
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
        val newTrackPoint = TrackPoint(
            id = idGenerator.generate(),
            latitude = location.latitude,
            longitude = location.longitude,
            timestampMs = timestampMs,
            speedKmh = speedKmh,
            altitudeMeters = location.altitudeMeters,
            isSegmentStart = false,
            accuracyMeters = location.accuracyMeters,
        )
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
