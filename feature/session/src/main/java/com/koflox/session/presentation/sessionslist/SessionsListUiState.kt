package com.koflox.session.presentation.sessionslist

import android.content.Intent
import com.koflox.session.presentation.share.SharePreviewData

internal sealed interface SessionsListUiState {
    data object Loading : SessionsListUiState

    data class Content(
        val sessions: List<SessionListItemUiModel>,
        val overlay: SessionsListOverlay? = null,
    ) : SessionsListUiState

    data object Empty : SessionsListUiState
}

internal sealed interface SessionsListOverlay {
    data class SharePreview(val data: SharePreviewData) : SessionsListOverlay
    data class Sharing(val data: SharePreviewData) : SessionsListOverlay
    data class ShareReady(val intent: Intent) : SessionsListOverlay
    data class ShareError(val message: String, val data: SharePreviewData) : SessionsListOverlay
    data class LoadError(val message: String) : SessionsListOverlay
}

data class SessionListItemUiModel(
    val id: String,
    val destinationName: String,
    val dateFormatted: String,
    val distanceFormatted: String,
    val status: SessionListItemStatus,
    val isShareButtonVisible: Boolean,
)

enum class SessionListItemStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
}
