package com.koflox.session.presentation.completion

import android.content.Intent
import com.koflox.designsystem.text.UiText
import com.koflox.session.presentation.model.DisplayStat
import com.koflox.session.presentation.route.MapLayer
import com.koflox.session.presentation.route.RouteDisplayData
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
        val averagePowerFormatted: String? = null,
        val maxPowerFormatted: String? = null,
        val completedStats: List<DisplayStat> = emptyList(),
        val shareStats: List<DisplayStat> = emptyList(),
        val selectedLayer: MapLayer = MapLayer.DEFAULT,
        val availableLayers: List<MapLayer>,
        val routeDisplayData: RouteDisplayData,
        val endMarkerRotation: Float = 0f,
        val overlay: Overlay? = null,
    ) : SessionCompletionUiState

    data class Error(val message: UiText) : SessionCompletionUiState
}

internal sealed interface Overlay {
    data class ShareDialog(val sharePreviewData: SharePreviewData) : Overlay
    data class Sharing(val sharePreviewData: SharePreviewData) : Overlay
    data class ShareReady(val intent: Intent) : Overlay
    data class ShareError(val message: UiText) : Overlay
}
