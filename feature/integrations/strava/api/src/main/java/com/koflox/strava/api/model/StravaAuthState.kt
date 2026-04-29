package com.koflox.strava.api.model

sealed interface StravaAuthState {

    data object LoggedOut : StravaAuthState

    data class LoggedIn(
        val athleteId: Long,
        val athleteName: String,
    ) : StravaAuthState
}
