package com.koflox.session.testutil

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.usecase.CreateSessionParams

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
    totalPowerReadings: Int? = null,
    sumPowerWatts: Long? = null,
    maxPowerWatts: Int? = null,
    totalEnergyJoules: Double? = null,
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
    totalPowerReadings = totalPowerReadings,
    sumPowerWatts = sumPowerWatts,
    maxPowerWatts = maxPowerWatts,
    totalEnergyJoules = totalEnergyJoules,
)

fun createTrackPoint(
    pointIndex: Int = 0,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    timestampMs: Long = 0L,
    speedKmh: Double = 0.0,
    altitudeMeters: Double? = null,
    isSegmentStart: Boolean = false,
    accuracyMeters: Float? = null,
    powerWatts: Int? = null,
) = TrackPoint(
    pointIndex = pointIndex,
    latitude = latitude,
    longitude = longitude,
    timestampMs = timestampMs,
    speedKmh = speedKmh,
    altitudeMeters = altitudeMeters,
    isSegmentStart = isSegmentStart,
    accuracyMeters = accuracyMeters,
    powerWatts = powerWatts,
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
    totalPowerReadings: Int? = null,
    sumPowerWatts: Long? = null,
    maxPowerWatts: Int? = null,
    totalEnergyJoules: Double? = null,
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
    totalPowerReadings = totalPowerReadings,
    sumPowerWatts = sumPowerWatts,
    maxPowerWatts = maxPowerWatts,
    totalEnergyJoules = totalEnergyJoules,
)

fun createTrackPointEntity(
    sessionId: String = "",
    pointIndex: Int = 0,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    timestampMs: Long = 0L,
    speedKmh: Double = 0.0,
    altitudeMeters: Double? = null,
    isSegmentStart: Boolean = false,
    accuracyMeters: Float? = null,
    powerWatts: Int? = null,
) = TrackPointEntity(
    sessionId = sessionId,
    pointIndex = pointIndex,
    latitude = latitude,
    longitude = longitude,
    timestampMs = timestampMs,
    speedKmh = speedKmh,
    altitudeMeters = altitudeMeters,
    isSegmentStart = isSegmentStart,
    accuracyMeters = accuracyMeters,
    powerWatts = powerWatts,
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
