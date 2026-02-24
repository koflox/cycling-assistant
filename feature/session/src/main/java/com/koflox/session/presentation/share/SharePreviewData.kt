package com.koflox.session.presentation.share

import com.koflox.session.presentation.route.RouteDisplayData

data class SharePreviewData(
    val sessionId: String,
    val destinationName: String?,
    val startDateFormatted: String,
    val elapsedTimeFormatted: String,
    val movingTimeFormatted: String,
    val idleTimeFormatted: String,
    val traveledDistanceFormatted: String,
    val averageSpeedFormatted: String,
    val topSpeedFormatted: String,
    val altitudeGainFormatted: String,
    val altitudeLossFormatted: String,
    val caloriesFormatted: String?,
    val routeDisplayData: RouteDisplayData,
    val endMarkerRotation: Float = 0f,
)
