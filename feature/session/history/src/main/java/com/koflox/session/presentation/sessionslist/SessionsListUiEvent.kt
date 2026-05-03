package com.koflox.session.presentation.sessionslist

internal sealed interface SessionsListUiEvent {
    data object LoadErrorDismissed : SessionsListUiEvent
    data object ToastDismissed : SessionsListUiEvent
    data object TransientToastShown : SessionsListUiEvent

    data class MenuRequested(val sessionId: String) : SessionsListUiEvent
    data object MenuDismissed : SessionsListUiEvent

    data class DeleteRequested(val sessionId: String) : SessionsListUiEvent
    data object DeleteDismissed : SessionsListUiEvent
    data class DeleteConfirmed(val sessionId: String) : SessionsListUiEvent

    data class RenameRequested(val sessionId: String) : SessionsListUiEvent
    data object RenameDismissed : SessionsListUiEvent
    data class RenameInputChanged(val input: String) : SessionsListUiEvent
    data object RenameConfirmed : SessionsListUiEvent
}
