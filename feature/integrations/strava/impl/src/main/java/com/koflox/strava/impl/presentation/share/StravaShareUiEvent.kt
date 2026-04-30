package com.koflox.strava.impl.presentation.share

internal sealed interface StravaShareUiEvent {
    data class Started(val sessionId: String) : StravaShareUiEvent
    data object ConnectClicked : StravaShareUiEvent
    data object SyncClicked : StravaShareUiEvent
    data object RetryClicked : StravaShareUiEvent
    data object RefreshClicked : StravaShareUiEvent
    data object ViewOnStravaClicked : StravaShareUiEvent
}
