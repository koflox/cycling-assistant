package com.koflox.session.domain.model

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val timestampMs: Long,
    val speedKmh: Double,
    val altitudeMeters: Double?,
)
