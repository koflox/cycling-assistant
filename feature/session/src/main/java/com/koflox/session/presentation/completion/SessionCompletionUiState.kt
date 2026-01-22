package com.koflox.session.presentation.completion

import android.content.Intent
import com.google.android.gms.maps.model.LatLng

internal sealed interface SessionCompletionUiState {

    data object Loading : SessionCompletionUiState

    data class Content(
        val sessionId: String,
        val destinationName: String,
        val startDateFormatted: String,
        val elapsedTimeFormatted: String,
        val traveledDistanceFormatted: String,
        val averageSpeedFormatted: String,
        val topSpeedFormatted: String,
        val routePoints: List<LatLng>,
        val overlay: Overlay? = null,
    ) : SessionCompletionUiState

    data class Error(val message: String) : SessionCompletionUiState
}

internal sealed interface Overlay {
    data object ShareDialog : Overlay
    data object Sharing : Overlay
    data class ShareReady(val intent: Intent) : Overlay
    data class ShareError(val message: String) : Overlay
}
