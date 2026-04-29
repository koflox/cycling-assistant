package com.koflox.gpx

data class GpxInput(
    val name: String,
    val startTimeMs: Long,
    val trackPoints: List<GpxTrackPoint>,
)

data class GpxTrackPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val timestampMs: Long,
    val powerWatts: Int?,
    val isSegmentStart: Boolean,
)
