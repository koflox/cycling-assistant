package com.koflox.strava.api.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface StravaShareTabNavigator {

    @Composable
    fun StravaTab(
        sessionId: String,
        modifier: Modifier,
    )
}
