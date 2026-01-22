package com.koflox.session.presentation.share

import com.google.android.gms.maps.model.LatLng

data class SharePreviewData(
    val sessionId: String,
    val destinationName: String,
    val startDateFormatted: String,
    val elapsedTimeFormatted: String,
    val traveledDistanceFormatted: String,
    val averageSpeedFormatted: String,
    val topSpeedFormatted: String,
    val routePoints: List<LatLng>,
)
