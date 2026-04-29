package com.koflox.strava.impl.presentation.connect

internal sealed interface StravaConnectNavigation {
    data object LaunchOAuthIntent : StravaConnectNavigation
}
