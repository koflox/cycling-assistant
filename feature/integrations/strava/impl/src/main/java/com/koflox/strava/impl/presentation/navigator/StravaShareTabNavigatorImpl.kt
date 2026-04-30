package com.koflox.strava.impl.presentation.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.strava.api.navigator.StravaShareTabNavigator
import com.koflox.strava.impl.presentation.share.StravaShareTabRoute
import javax.inject.Inject

internal class StravaShareTabNavigatorImpl @Inject constructor() : StravaShareTabNavigator {

    @Composable
    override fun StravaTab(sessionId: String, modifier: Modifier) {
        StravaShareTabRoute(
            sessionId = sessionId,
            modifier = modifier,
        )
    }
}
