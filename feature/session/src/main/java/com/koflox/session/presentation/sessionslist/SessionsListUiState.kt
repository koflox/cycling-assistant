package com.koflox.session.presentation.sessionslist

import android.content.Intent
import com.koflox.session.presentation.share.SharePreviewData

data class SessionsListUiState(
    val sessions: List<SessionListItemUiModel> = emptyList(),
    val isEmpty: Boolean = true,
    val sharePreviewData: SharePreviewData? = null,
    val isSharing: Boolean = false,
    val shareIntent: Intent? = null,
    val shareError: String? = null,
)

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
