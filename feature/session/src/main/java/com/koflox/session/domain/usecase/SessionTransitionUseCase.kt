package com.koflox.session.domain.usecase

import com.koflox.concurrent.suspendRunCatching
import com.koflox.distance.DistanceCalculator
import com.koflox.id.IdGenerator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository

interface SessionTransitionUseCase {

    // TODO: rename to create() and extract into a separate UC
    suspend fun start(params: SessionStartParams): Result<Session>

    // TODO: pass session id only and make a common method for changing its status, removing dedicated resume method
    fun pause(session: Session): Session

    fun resume(session: Session): Session

    suspend fun stop(session: Session): Result<String>

    // TODO: extract into a separate UC
    fun updateLocation(
        session: Session,
        latitude: Double,
        longitude: Double,
        timestampMs: Long,
    ): Session
}

data class SessionStartParams(
    val destinationId: String,
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val startLatitude: Double,
    val startLongitude: Double,
)

internal class SessionTransitionUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val distanceCalculator: DistanceCalculator,
    private val idGenerator: IdGenerator,
) : SessionTransitionUseCase {

    override suspend fun start(params: SessionStartParams): Result<Session> = suspendRunCatching {
        val currentTimeMs = System.currentTimeMillis()
        Session(
            id = idGenerator.generate(),
            destinationId = params.destinationId,
            destinationName = params.destinationName,
            destinationLatitude = params.destinationLatitude,
            destinationLongitude = params.destinationLongitude,
            startLatitude = params.startLatitude,
            startLongitude = params.startLongitude,
            startTimeMs = currentTimeMs,
            endTimeMs = null,
            elapsedTimeMs = 0L,
            traveledDistanceKm = 0.0,
            averageSpeedKmh = 0.0,
            topSpeedKmh = 0.0,
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                TrackPoint(
                    latitude = params.startLatitude,
                    longitude = params.startLongitude,
                    timestampMs = currentTimeMs,
                    speedKmh = 0.0,
                ),
            ),
        )
    }

    override fun pause(session: Session): Session = session.copy(
        status = SessionStatus.PAUSED,
    )

    override fun resume(session: Session): Session = session.copy(
        status = SessionStatus.RUNNING,
    )

    override suspend fun stop(session: Session): Result<String> {
        val completedSession = session.copy(
            status = SessionStatus.COMPLETED,
            endTimeMs = System.currentTimeMillis(),
        )
        return sessionRepository.saveSession(completedSession).map { session.id }
    }

    override fun updateLocation(
        session: Session,
        latitude: Double,
        longitude: Double,
        timestampMs: Long,
    ): Session {
        if (session.status != SessionStatus.RUNNING) {
            return session
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
        val newTrackPoint = TrackPoint(
            latitude = latitude,
            longitude = longitude,
            timestampMs = timestampMs,
            speedKmh = speedKmh,
        )
        val updatedTrackPoints = session.trackPoints + newTrackPoint
        val totalDistanceKm = session.traveledDistanceKm + distanceKm
        val newTopSpeedKmh = maxOf(session.topSpeedKmh, speedKmh)
        val elapsedTimeMs = timestampMs - session.startTimeMs

        val averageSpeedKmh = if (elapsedTimeMs > 0) {
            (totalDistanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR
        } else {
            0.0
        }

        return session.copy(
            elapsedTimeMs = elapsedTimeMs,
            traveledDistanceKm = totalDistanceKm,
            averageSpeedKmh = averageSpeedKmh,
            topSpeedKmh = newTopSpeedKmh,
            trackPoints = updatedTrackPoints,
        )
    }

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
    }
}
