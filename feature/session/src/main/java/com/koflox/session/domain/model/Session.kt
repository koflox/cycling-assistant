package com.koflox.session.domain.model

data class Session(
    val id: String,
    val destinationId: String,
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val startLatitude: Double,
    val startLongitude: Double,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val elapsedTimeMs: Long,
    val traveledDistanceKm: Double,
    val averageSpeedKmh: Double,
    val topSpeedKmh: Double,
    val status: SessionStatus,
    val trackPoints: List<TrackPoint>,
)
