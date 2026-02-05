package com.koflox.session.domain.model

data class TrackPoint(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val timestampMs: Long,
    val speedKmh: Double,
    val altitudeMeters: Double?,
    val isSegmentStart: Boolean,
    val accuracyMeters: Float?,
)
