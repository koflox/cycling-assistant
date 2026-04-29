package com.koflox.session.presentation.share

import com.koflox.gpx.GpxInput
import com.koflox.gpx.GpxTrackPoint
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.TrackPoint

private const val FREE_ROAM_NAME = "Free Roam"

fun Session.toGpxInput(): GpxInput = GpxInput(
    name = destinationName ?: FREE_ROAM_NAME,
    startTimeMs = startTimeMs,
    trackPoints = trackPoints.map { it.toGpxTrackPoint() },
)

private fun TrackPoint.toGpxTrackPoint(): GpxTrackPoint = GpxTrackPoint(
    latitude = latitude,
    longitude = longitude,
    altitudeMeters = altitudeMeters,
    timestampMs = timestampMs,
    powerWatts = powerWatts,
    isSegmentStart = isSegmentStart,
)
