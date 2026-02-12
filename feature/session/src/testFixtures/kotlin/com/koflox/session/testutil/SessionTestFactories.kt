package com.koflox.session.testutil

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.usecase.CreateSessionParams
import com.koflox.session.presentation.mapper.SessionUiModel
import com.koflox.session.presentation.sessionslist.SessionListItemStatus
import com.koflox.session.presentation.sessionslist.SessionListItemUiModel

fun createSession(
    id: String = "",
    destinationId: String? = "",
    destinationName: String? = "",
    destinationLatitude: Double? = 0.0,
    destinationLongitude: Double? = 0.0,
    startLatitude: Double = 0.0,
    startLongitude: Double = 0.0,
    startTimeMs: Long = 0L,
    lastResumedTimeMs: Long = 0L,
    endTimeMs: Long? = null,
    elapsedTimeMs: Long = 0L,
    traveledDistanceKm: Double = 0.0,
    averageSpeedKmh: Double = 0.0,
    topSpeedKmh: Double = 0.0,
    totalAltitudeGainMeters: Double = 0.0,
    status: SessionStatus = SessionStatus.RUNNING,
    trackPoints: List<TrackPoint> = emptyList(),
) = Session(
    id = id,
    destinationId = destinationId,
    destinationName = destinationName,
    destinationLatitude = destinationLatitude,
    destinationLongitude = destinationLongitude,
    startLatitude = startLatitude,
    startLongitude = startLongitude,
    startTimeMs = startTimeMs,
    lastResumedTimeMs = lastResumedTimeMs,
    endTimeMs = endTimeMs,
    elapsedTimeMs = elapsedTimeMs,
    traveledDistanceKm = traveledDistanceKm,
    averageSpeedKmh = averageSpeedKmh,
    topSpeedKmh = topSpeedKmh,
    totalAltitudeGainMeters = totalAltitudeGainMeters,
    status = status,
    trackPoints = trackPoints,
)

fun createTrackPoint(
    id: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    timestampMs: Long = 0L,
    speedKmh: Double = 0.0,
    altitudeMeters: Double? = null,
    isSegmentStart: Boolean = false,
    accuracyMeters: Float? = null,
) = TrackPoint(
    id = id,
    latitude = latitude,
    longitude = longitude,
    timestampMs = timestampMs,
    speedKmh = speedKmh,
    altitudeMeters = altitudeMeters,
    isSegmentStart = isSegmentStart,
    accuracyMeters = accuracyMeters,
)

fun createSessionEntity(
    id: String = "",
    destinationId: String? = "",
    destinationName: String? = "",
    destinationLatitude: Double? = 0.0,
    destinationLongitude: Double? = 0.0,
    startLatitude: Double = 0.0,
    startLongitude: Double = 0.0,
    startTimeMs: Long = 0L,
    lastResumedTimeMs: Long = 0L,
    endTimeMs: Long? = null,
    elapsedTimeMs: Long = 0L,
    traveledDistanceKm: Double = 0.0,
    averageSpeedKmh: Double = 0.0,
    topSpeedKmh: Double = 0.0,
    totalAltitudeGainMeters: Double = 0.0,
    status: String = "",
) = SessionEntity(
    id = id,
    destinationId = destinationId,
    destinationName = destinationName,
    destinationLatitude = destinationLatitude,
    destinationLongitude = destinationLongitude,
    startLatitude = startLatitude,
    startLongitude = startLongitude,
    startTimeMs = startTimeMs,
    lastResumedTimeMs = lastResumedTimeMs,
    endTimeMs = endTimeMs,
    elapsedTimeMs = elapsedTimeMs,
    traveledDistanceKm = traveledDistanceKm,
    averageSpeedKmh = averageSpeedKmh,
    topSpeedKmh = topSpeedKmh,
    totalAltitudeGainMeters = totalAltitudeGainMeters,
    status = status,
)

fun createTrackPointEntity(
    id: String = "",
    sessionId: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    timestampMs: Long = 0L,
    speedKmh: Double = 0.0,
    altitudeMeters: Double? = null,
    isSegmentStart: Boolean = false,
    accuracyMeters: Float? = null,
) = TrackPointEntity(
    id = id,
    sessionId = sessionId,
    latitude = latitude,
    longitude = longitude,
    timestampMs = timestampMs,
    speedKmh = speedKmh,
    altitudeMeters = altitudeMeters,
    isSegmentStart = isSegmentStart,
    accuracyMeters = accuracyMeters,
)

fun createSessionWithTrackPoints(
    session: SessionEntity = createSessionEntity(),
    trackPoints: List<TrackPointEntity> = emptyList(),
) = SessionWithTrackPoints(
    session = session,
    trackPoints = trackPoints,
)

fun createFreeRoamSessionParams(): CreateSessionParams = CreateSessionParams.FreeRoam

fun createDestinationSessionParams(
    destinationId: String = "",
    destinationName: String = "",
    destinationLatitude: Double = 0.0,
    destinationLongitude: Double = 0.0,
) = CreateSessionParams.Destination(
    destinationId = destinationId,
    destinationName = destinationName,
    destinationLatitude = destinationLatitude,
    destinationLongitude = destinationLongitude,
)

fun createSessionUiModel(
    elapsedTimeFormatted: String = "",
    traveledDistanceFormatted: String = "",
    averageSpeedFormatted: String = "",
    topSpeedFormatted: String = "",
    altitudeGainFormatted: String = "",
) = SessionUiModel(
    elapsedTimeFormatted = elapsedTimeFormatted,
    traveledDistanceFormatted = traveledDistanceFormatted,
    averageSpeedFormatted = averageSpeedFormatted,
    topSpeedFormatted = topSpeedFormatted,
    altitudeGainFormatted = altitudeGainFormatted,
)

fun createSessionListItemUiModel(
    id: String = "",
    destinationName: String = "",
    dateFormatted: String = "",
    distanceFormatted: String = "",
    status: SessionListItemStatus = SessionListItemStatus.COMPLETED,
    isShareButtonVisible: Boolean = false,
) = SessionListItemUiModel(
    id = id,
    destinationName = destinationName,
    dateFormatted = dateFormatted,
    distanceFormatted = distanceFormatted,
    status = status,
    isShareButtonVisible = isShareButtonVisible,
)
