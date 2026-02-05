package com.koflox.session.presentation.session

internal sealed interface SessionUiEvent {
    sealed interface SessionManagementEvent : SessionUiEvent {
        data object PauseClicked : SessionManagementEvent
        data object ResumeClicked : SessionManagementEvent
        data object StopClicked : SessionManagementEvent
        data object StopConfirmationDismissed : SessionManagementEvent
        data object StopConfirmed : SessionManagementEvent
        data object ErrorDismissed : SessionManagementEvent
    }

    sealed interface LocationSettingsEvent : SessionUiEvent {
        data object EnableLocationClicked : LocationSettingsEvent
        data object LocationEnabled : LocationSettingsEvent
        data object LocationEnableDenied : LocationSettingsEvent
        data object LocationDisabledDismissed : LocationSettingsEvent
    }
}
