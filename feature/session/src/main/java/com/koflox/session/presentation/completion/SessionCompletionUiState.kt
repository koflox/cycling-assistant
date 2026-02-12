package com.koflox.session.presentation.completion

import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import com.koflox.session.presentation.share.SharePreviewData

internal sealed interface SessionCompletionUiState {

    data object Loading : SessionCompletionUiState

    data class Content(
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
        val routePoints: List<LatLng>,
        val startMarkerRotation: Float = 0f,
        val endMarkerRotation: Float = 0f,
        val overlay: Overlay? = null,
    ) : SessionCompletionUiState

    data class Error(val message: String) : SessionCompletionUiState
}

internal sealed interface Overlay {
    data class ShareDialog(val sharePreviewData: SharePreviewData) : Overlay
    data class Sharing(val sharePreviewData: SharePreviewData) : Overlay
    data class ShareReady(val intent: Intent) : Overlay
    data class ShareError(val message: String) : Overlay
}
