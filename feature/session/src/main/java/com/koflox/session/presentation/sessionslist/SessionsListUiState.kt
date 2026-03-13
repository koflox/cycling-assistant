package com.koflox.session.presentation.sessionslist

import com.koflox.designsystem.text.UiText

internal sealed interface SessionsListUiState {
    data object Loading : SessionsListUiState

    data class Content(
        val sessions: List<SessionListItemUiModel>,
        val overlay: SessionsListOverlay? = null,
    ) : SessionsListUiState

    data object Empty : SessionsListUiState
}

internal sealed interface SessionsListOverlay {
    data class LoadError(val message: UiText) : SessionsListOverlay
}

internal data class SessionListItemUiModel(
    val id: String,
    val destinationName: String?,
    val dateFormatted: String,
    val distanceFormatted: String,
    val status: SessionListItemStatus,
    val isShareButtonVisible: Boolean,
)

internal enum class SessionListItemStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
}
