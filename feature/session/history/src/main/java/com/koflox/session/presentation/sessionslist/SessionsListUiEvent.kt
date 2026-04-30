package com.koflox.session.presentation.sessionslist

internal sealed interface SessionsListUiEvent {
    data object LoadErrorDismissed : SessionsListUiEvent
}
