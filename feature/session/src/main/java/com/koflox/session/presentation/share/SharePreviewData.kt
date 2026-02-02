package com.koflox.session.presentation.share

import com.google.android.gms.maps.model.LatLng

data class SharePreviewData(
    val sessionId: String,
    val destinationName: String,
    val startDateFormatted: String,
    val elapsedTimeFormatted: String,
    val movingTimeFormatted: String,
    val idleTimeFormatted: String,
    val traveledDistanceFormatted: String,
    val averageSpeedFormatted: String,
    val topSpeedFormatted: String,
    val altitudeGainFormatted: String,
    val altitudeLossFormatted: String,
    val caloriesFormatted: String,
    val routePoints: List<LatLng>,
)
