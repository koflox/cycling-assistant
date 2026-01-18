package com.koflox.session.presentation.session

sealed interface SessionUiEvent {
    data object PauseClicked : SessionUiEvent
    data object ResumeClicked : SessionUiEvent
    data object StopClicked : SessionUiEvent
    data object StopConfirmationDismissed : SessionUiEvent
    data object StopConfirmed : SessionUiEvent
    data object CompletedSessionNavigated : SessionUiEvent
    data object ErrorDismissed : SessionUiEvent
}
