package com.koflox.session.data.mapper

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SessionMapperImpl(
    private val dispatcherDefault: CoroutineDispatcher,
) : SessionMapper {

    override suspend fun toEntity(session: Session): SessionEntity = withContext(dispatcherDefault) {
        SessionEntity(
            id = session.id,
            destinationId = session.destinationId,
            destinationName = session.destinationName,
            destinationLatitude = session.destinationLatitude,
            destinationLongitude = session.destinationLongitude,
            startLatitude = session.startLatitude,
            startLongitude = session.startLongitude,
            startTimeMs = session.startTimeMs,
            endTimeMs = session.endTimeMs,
            elapsedTimeMs = session.elapsedTimeMs,
            traveledDistanceKm = session.traveledDistanceKm,
            averageSpeedKmh = session.averageSpeedKmh,
            topSpeedKmh = session.topSpeedKmh,
            status = session.status.name,
        )
    }

    override suspend fun toTrackPointEntities(
        sessionId: String,
        trackPoints: List<TrackPoint>,
    ): List<TrackPointEntity> = withContext(dispatcherDefault) {
        trackPoints.map { trackPoint ->
            TrackPointEntity(
                sessionId = sessionId,
                latitude = trackPoint.latitude,
                longitude = trackPoint.longitude,
                timestampMs = trackPoint.timestampMs,
                speedKmh = trackPoint.speedKmh,
            )
        }
    }

    override suspend fun toDomain(sessionWithTrackPoints: SessionWithTrackPoints): Session = withContext(dispatcherDefault) {
        val entity = sessionWithTrackPoints.session
        val trackPoints = sessionWithTrackPoints.trackPoints
        Session(
            id = entity.id,
            destinationId = entity.destinationId,
            destinationName = entity.destinationName,
            destinationLatitude = entity.destinationLatitude,
            destinationLongitude = entity.destinationLongitude,
            startLatitude = entity.startLatitude,
            startLongitude = entity.startLongitude,
            startTimeMs = entity.startTimeMs,
            endTimeMs = entity.endTimeMs,
            elapsedTimeMs = entity.elapsedTimeMs,
            traveledDistanceKm = entity.traveledDistanceKm,
            averageSpeedKmh = entity.averageSpeedKmh,
            topSpeedKmh = entity.topSpeedKmh,
            status = SessionStatus.valueOf(entity.status),
            trackPoints = trackPoints.map { trackPointEntity ->
                TrackPoint(
                    latitude = trackPointEntity.latitude,
                    longitude = trackPointEntity.longitude,
                    timestampMs = trackPointEntity.timestampMs,
                    speedKmh = trackPointEntity.speedKmh,
                )
            },
        )
    }

    override suspend fun toDomainList(sessionsWithTrackPoints: List<SessionWithTrackPoints>): List<Session> {
        return withContext(dispatcherDefault) {
            sessionsWithTrackPoints.map { sessionWithTrackPoints ->
                val entity = sessionWithTrackPoints.session
                val trackPoints = sessionWithTrackPoints.trackPoints
                Session(
                    id = entity.id,
                    destinationId = entity.destinationId,
                    destinationName = entity.destinationName,
                    destinationLatitude = entity.destinationLatitude,
                    destinationLongitude = entity.destinationLongitude,
                    startLatitude = entity.startLatitude,
                    startLongitude = entity.startLongitude,
                    startTimeMs = entity.startTimeMs,
                    endTimeMs = entity.endTimeMs,
                    elapsedTimeMs = entity.elapsedTimeMs,
                    traveledDistanceKm = entity.traveledDistanceKm,
                    averageSpeedKmh = entity.averageSpeedKmh,
                    topSpeedKmh = entity.topSpeedKmh,
                    status = SessionStatus.valueOf(entity.status),
                    trackPoints = trackPoints.map { trackPointEntity ->
                        TrackPoint(
                            latitude = trackPointEntity.latitude,
                            longitude = trackPointEntity.longitude,
                            timestampMs = trackPointEntity.timestampMs,
                            speedKmh = trackPointEntity.speedKmh,
                        )
                    },
                )
            }
        }
    }

}
