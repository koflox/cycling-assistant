package com.koflox.strava.impl.presentation.share

internal sealed interface StravaShareNavigation {
    data object LaunchOAuthIntent : StravaShareNavigation
    data class OpenStravaActivity(val activityId: Long) : StravaShareNavigation
}
