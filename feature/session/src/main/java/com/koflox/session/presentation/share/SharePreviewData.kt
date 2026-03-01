package com.koflox.session.presentation.share

import com.koflox.session.presentation.model.DisplayStat
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
    val averagePowerFormatted: String? = null,
    val maxPowerFormatted: String? = null,
    val shareStats: List<DisplayStat> = emptyList(),
    val routeDisplayData: RouteDisplayData,
    val endMarkerRotation: Float = 0f,
)
