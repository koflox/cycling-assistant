package com.koflox.session.domain.usecase.comparison

import com.koflox.altitude.AltitudeCalculator
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.location.smoother.LocationSmoother
import com.koflox.location.validator.LocationValidator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint

/**
 * V3 — Kalman filter + median speed smoothing (no acceleration clamping).
 *
 * Added a 1-D Kalman filter per coordinate axis to smooth GPS jitter,
 * a 5-sample moving-median speed buffer, and a stationary speed threshold
 * (< 1 km/h discarded). Displacement is also checked against GPS accuracy.
 *
 * Unlike the current V4 implementation, V3 does **not** apply acceleration
 * clamping — raw speed values go directly into the median buffer.
 */
internal class V3KalmanComparisonLocationProcessor(
    private val distanceCalculator: DistanceCalculator,
    private val altitudeCalculator: AltitudeCalculator,
    private val locationValidator: LocationValidator,
    private val locationSmoother: LocationSmoother,
) : ComparisonLocationProcessor {

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
        private const val MIN_DISTANCE_METERS = 5.0
        private const val MAX_SPEED_KMH = 100.0
        private const val METERS_PER_KM = 1000.0
        private const val STATIONARY_SPEED_THRESHOLD_KMH = 1.0
        private const val SPEED_BUFFER_SIZE = 5
        private const val VERSION_TAG = "v3"
        private const val VERSION_LABEL = "V3 Kalman"
    }

    override val versionTag = VERSION_TAG
    override val versionLabel = VERSION_LABEL

    private var session: Session? = null
    private val speedBuffer = ArrayDeque<Double>(SPEED_BUFFER_SIZE)

    override fun initialize(baseSession: Session) {
        session = baseSession.copy(
            id = "${baseSession.id}_v3",
            destinationName = baseSession.destinationName?.let { "$it ($VERSION_LABEL)" } ?: VERSION_LABEL,
            trackPoints = emptyList(),
        )
        speedBuffer.clear()
        locationSmoother.reset()
    }

    override fun update(location: Location, timestampMs: Long, lastResumedTimeMs: Long) {
        val current = session
        if (!locationValidator.isAccuracyValid(location) || current == null || current.status != SessionStatus.RUNNING) return
        val updated = current.copy(lastResumedTimeMs = lastResumedTimeMs)
        val previousTrackPoint = updated.trackPoints.lastOrNull()

        if (previousTrackPoint == null || previousTrackPoint.timestampMs < lastResumedTimeMs) {
            saveSegmentStartPoint(updated, location, timestampMs)
        } else {
            processNormalPoint(updated, previousTrackPoint, location, timestampMs)
        }
    }

    override fun getSession(): Session? = session

    override fun reset() {
        session = null
        speedBuffer.clear()
        locationSmoother.reset()
    }

    private fun saveSegmentStartPoint(currentSession: Session, location: Location, timestampMs: Long) {
        locationSmoother.reset()
        speedBuffer.clear()
        val smoothedLocation = locationSmoother.smooth(location, timestampMs)
        val newTrackPoint = TrackPoint(
            pointIndex = currentSession.trackPoints.size,
            latitude = smoothedLocation.latitude,
            longitude = smoothedLocation.longitude,
            timestampMs = timestampMs,
            speedKmh = 0.0,
            altitudeMeters = location.altitudeMeters,
            isSegmentStart = true,
            accuracyMeters = location.accuracyMeters,
        )
        val elapsedTimeMs = currentSession.elapsedTimeMs + (timestampMs - currentSession.lastResumedTimeMs)
        session = currentSession.copy(
            elapsedTimeMs = elapsedTimeMs,
            lastResumedTimeMs = timestampMs,
            averageSpeedKmh = if (elapsedTimeMs > 0) {
                (currentSession.traveledDistanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR
            } else {
                0.0
            },
            trackPoints = currentSession.trackPoints + newTrackPoint,
        )
    }

    @Suppress("ReturnCount")
    private fun processNormalPoint(
        currentSession: Session,
        previousTrackPoint: TrackPoint,
        location: Location,
        timestampMs: Long,
    ) {
        val smoothedLocation = locationSmoother.smooth(location, timestampMs)
        val distanceKm = distanceCalculator.calculateKm(
            previousTrackPoint.latitude, previousTrackPoint.longitude,
            smoothedLocation.latitude, smoothedLocation.longitude,
        )
        val displacementMeters = distanceKm * METERS_PER_KM
        val accuracy = location.accuracyMeters
        if (accuracy != null && displacementMeters < accuracy) return
        val timeDiffMs = timestampMs - previousTrackPoint.timestampMs
        val rawSpeedKmh = if (timeDiffMs > 0) (distanceKm / timeDiffMs) * MILLISECONDS_PER_HOUR else 0.0
        if (!isLocationValid(distanceKm, rawSpeedKmh)) return
        val smoothedSpeedKmh = medianSmoothedSpeed(rawSpeedKmh)
        if (smoothedSpeedKmh < STATIONARY_SPEED_THRESHOLD_KMH) return
        val altitudeGain = altitudeCalculator.calculateGain(
            previousTrackPoint.altitudeMeters, location.altitudeMeters,
        )
        val newTrackPoint = TrackPoint(
            pointIndex = currentSession.trackPoints.size,
            latitude = smoothedLocation.latitude,
            longitude = smoothedLocation.longitude,
            timestampMs = timestampMs,
            speedKmh = smoothedSpeedKmh,
            altitudeMeters = location.altitudeMeters,
            isSegmentStart = false,
            accuracyMeters = location.accuracyMeters,
        )
        val totalDistanceKm = currentSession.traveledDistanceKm + distanceKm
        val elapsedTimeMs = currentSession.elapsedTimeMs + (timestampMs - currentSession.lastResumedTimeMs)
        session = currentSession.copy(
            elapsedTimeMs = elapsedTimeMs,
            lastResumedTimeMs = timestampMs,
            traveledDistanceKm = totalDistanceKm,
            averageSpeedKmh = if (elapsedTimeMs > 0) {
                (totalDistanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR
            } else {
                0.0
            },
            topSpeedKmh = if (speedBuffer.size >= SPEED_BUFFER_SIZE) {
                maxOf(currentSession.topSpeedKmh, smoothedSpeedKmh)
            } else {
                currentSession.topSpeedKmh
            },
            totalAltitudeGainMeters = currentSession.totalAltitudeGainMeters + altitudeGain,
            trackPoints = currentSession.trackPoints + newTrackPoint,
        )
    }

    private fun isLocationValid(distanceKm: Double, speedKmh: Double): Boolean =
        distanceKm * METERS_PER_KM >= MIN_DISTANCE_METERS && speedKmh <= MAX_SPEED_KMH

    private fun medianSmoothedSpeed(rawSpeedKmh: Double): Double {
        if (speedBuffer.size >= SPEED_BUFFER_SIZE) {
            speedBuffer.removeFirst()
        }
        speedBuffer.addLast(rawSpeedKmh)
        val sorted = speedBuffer.sorted()
        return sorted[sorted.size / 2]
    }
}
