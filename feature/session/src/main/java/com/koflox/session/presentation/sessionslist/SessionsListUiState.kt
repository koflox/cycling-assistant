package com.koflox.session.presentation.sessionslist

data class SessionsListUiState(
    val sessions: List<SessionListItemUiModel> = emptyList(),
    val isEmpty: Boolean = true,
)

data class SessionListItemUiModel(
    val id: String,
    val destinationName: String,
    val dateFormatted: String,
    val distanceFormatted: String,
    val status: SessionListItemStatus,
)

enum class SessionListItemStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
}
