package com.koflox.session.domain.usecase.comparison

import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint

/**
 * V1 — Raw location recording.
 *
 * The earliest implementation: every GPS reading is recorded as-is with no
 * filtering, no smoothing, and no accuracy validation. Speed is computed
 * directly from raw coordinate deltas. Only an upper speed cap (100 km/h)
 * is applied to discard obviously invalid readings.
 */
internal class V1RawComparisonLocationProcessor(
    private val distanceCalculator: DistanceCalculator,
) : ComparisonLocationProcessor {

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
        private const val MAX_SPEED_KMH = 100.0
        private const val VERSION_TAG = "v1"
        private const val VERSION_LABEL = "V1 Raw"
    }

    override val versionTag = VERSION_TAG
    override val versionLabel = VERSION_LABEL

    private var session: Session? = null

    override fun initialize(baseSession: Session) {
        session = baseSession.copy(
            id = "${baseSession.id}_v1",
            destinationName = baseSession.destinationName?.let { "$it ($VERSION_LABEL)" } ?: VERSION_LABEL,
            trackPoints = emptyList(),
        )
    }

    override fun update(location: Location, timestampMs: Long, lastResumedTimeMs: Long) {
        val current = session ?: return
        if (current.status != SessionStatus.RUNNING) return
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
    }

    private fun saveSegmentStartPoint(currentSession: Session, location: Location, timestampMs: Long) {
        val newTrackPoint = TrackPoint(
            pointIndex = currentSession.trackPoints.size,
            latitude = location.latitude,
            longitude = location.longitude,
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

    private fun processNormalPoint(
        currentSession: Session,
        previousTrackPoint: TrackPoint,
        location: Location,
        timestampMs: Long,
    ) {
        val distanceKm = distanceCalculator.calculateKm(
            previousTrackPoint.latitude, previousTrackPoint.longitude,
            location.latitude, location.longitude,
        )
        val timeDiffMs = timestampMs - previousTrackPoint.timestampMs
        val speedKmh = if (timeDiffMs > 0) (distanceKm / timeDiffMs) * MILLISECONDS_PER_HOUR else 0.0
        if (speedKmh > MAX_SPEED_KMH) return
        val newTrackPoint = TrackPoint(
            pointIndex = currentSession.trackPoints.size,
            latitude = location.latitude,
            longitude = location.longitude,
            timestampMs = timestampMs,
            speedKmh = speedKmh,
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
            topSpeedKmh = maxOf(currentSession.topSpeedKmh, speedKmh),
            trackPoints = currentSession.trackPoints + newTrackPoint,
        )
    }
}
