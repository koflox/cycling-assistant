package com.koflox.session.presentation.sessionslist

internal sealed interface SessionsListUiEvent {
    data object LoadErrorDismissed : SessionsListUiEvent
    data class DeleteRequested(val sessionId: String) : SessionsListUiEvent
    data object DeleteDismissed : SessionsListUiEvent
    data class DeleteConfirmed(val sessionId: String) : SessionsListUiEvent
    data object ToastDismissed : SessionsListUiEvent
}
