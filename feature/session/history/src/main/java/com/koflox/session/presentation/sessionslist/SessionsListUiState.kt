package com.koflox.session.presentation.sessionslist

import com.koflox.designsystem.text.UiText
import com.koflox.session.domain.usecase.SessionNameValidation

internal sealed interface SessionsListUiState {
    data object Loading : SessionsListUiState

    data class Content(
        val sessions: List<SessionListItemUiModel>,
        val overlay: SessionsListOverlay? = null,
        val transientToast: UiText? = null,
    ) : SessionsListUiState

    data object Empty : SessionsListUiState
}

internal sealed interface SessionsListOverlay {
    data class LoadError(val message: UiText) : SessionsListOverlay
    data class Menu(val sessionId: String, val sessionName: String) : SessionsListOverlay
    data class DeleteConfirmation(val sessionId: String) : SessionsListOverlay
    data class RenamePrompt(
        val sessionId: String,
        val currentName: String,
        val input: String,
        val lastValidatedInput: String,
        val validation: SessionNameValidation,
    ) : SessionsListOverlay {
        val isSaveEnabled: Boolean
            get() = lastValidatedInput == input && validation is SessionNameValidation.Valid
    }
    data class Toast(val message: UiText) : SessionsListOverlay
}

internal data class SessionListItemUiModel(
    val id: String,
    val displayName: String,
    val dateFormatted: String,
    val distanceFormatted: String,
    val status: SessionListItemStatus,
    val isShareButtonVisible: Boolean,
) {
    val isCompleted: Boolean get() = status == SessionListItemStatus.COMPLETED
}

internal enum class SessionListItemStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
}
