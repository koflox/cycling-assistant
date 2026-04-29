package com.koflox.strava.impl.presentation.connect

internal sealed interface StravaConnectUiEvent {
    data object ConnectClicked : StravaConnectUiEvent
    data object LogoutClicked : StravaConnectUiEvent
    data object LogoutConfirmed : StravaConnectUiEvent
    data object LogoutDismissed : StravaConnectUiEvent
    data object HintDismissed : StravaConnectUiEvent
}
