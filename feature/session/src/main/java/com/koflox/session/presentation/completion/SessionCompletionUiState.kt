package com.koflox.session.presentation.completion

import android.content.Intent
import com.google.android.gms.maps.model.LatLng

data class SessionCompletionUiState(
    val sessionId: String,
    val isLoading: Boolean = true,
    val destinationName: String = "",
    val startDateFormatted: String = "",
    val elapsedTimeFormatted: String = "",
    val traveledDistanceFormatted: String = "",
    val averageSpeedFormatted: String = "",
    val topSpeedFormatted: String = "",
    val routePoints: List<LatLng> = emptyList(),
    val error: String? = null,
    val shouldNavigateToDashboard: Boolean = false,
    val showShareDialog: Boolean = false,
    val isSharing: Boolean = false,
    val shareIntent: Intent? = null,
)
