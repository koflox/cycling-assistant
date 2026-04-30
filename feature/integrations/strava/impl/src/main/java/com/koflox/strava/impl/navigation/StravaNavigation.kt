package com.koflox.strava.impl.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.koflox.strava.impl.presentation.connect.StravaConnectRoute

const val STRAVA_CONNECT_ROUTE = "strava_connect"

fun NavGraphBuilder.stravaConnectScreen(
    onBackClick: () -> Unit,
) {
    composable(route = STRAVA_CONNECT_ROUTE) {
        StravaConnectRoute(
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
