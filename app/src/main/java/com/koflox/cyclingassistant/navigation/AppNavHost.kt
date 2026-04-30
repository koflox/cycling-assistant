package com.koflox.cyclingassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.koflox.connections.navigation.CONNECTIONS_GRAPH_ROUTE
import com.koflox.connections.navigation.connectionsGraph
import com.koflox.dashboard.navigation.DASHBOARD_ROUTE
import com.koflox.dashboard.navigation.dashboardScreen
import com.koflox.poi.navigation.POI_SELECTION_ROUTE
import com.koflox.poi.navigation.poiSelectionScreen
import com.koflox.session.navigation.SESSIONS_GRAPH_ROUTE
import com.koflox.session.navigation.sessionCompletionRoute
import com.koflox.session.navigation.sessionGraph
import com.koflox.session.service.PendingSessionAction
import com.koflox.settings.navigation.SETTINGS_GRAPH_ROUTE
import com.koflox.settings.navigation.settingsGraph
import com.koflox.strava.impl.navigation.STRAVA_CONNECT_ROUTE
import com.koflox.strava.impl.navigation.stravaConnectScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppNavHostEntryPoint {
    fun pendingSessionAction(): PendingSessionAction
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DASHBOARD_ROUTE,
) {
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(context, AppNavHostEntryPoint::class.java)
    val pendingSessionAction = entryPoint.pendingSessionAction()
    val isStopRequested by pendingSessionAction.isStopRequested.collectAsState()
    LaunchedEffect(isStopRequested) {
        if (isStopRequested) {
            navController.popBackStack(DASHBOARD_ROUTE, inclusive = false)
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        appDestinations(navController)
    }
}

private fun NavGraphBuilder.appDestinations(navController: NavHostController) {
    dashboardScreen(
        onNavigateToSessionsList = { navController.navigate(SESSIONS_GRAPH_ROUTE) },
        onNavigateToConnections = { navController.navigate(CONNECTIONS_GRAPH_ROUTE) },
        onNavigateToSettings = { navController.navigate(SETTINGS_GRAPH_ROUTE) },
        onNavigateToSessionCompletion = { sessionId ->
            navController.navigate(sessionCompletionRoute(sessionId))
        },
        onNavigateToPoiSelection = { navController.navigate(POI_SELECTION_ROUTE) },
    )
    connectionsGraph(
        navController = navController,
        onBackClick = { navController.popBackStack() },
    )
    sessionGraph(
        navController = navController,
        onNavigateToDashboard = { navController.popBackStack(DASHBOARD_ROUTE, inclusive = false) },
        onBackFromList = { navController.popBackStack() },
    )
    poiSelectionScreen(
        onBackClick = { navController.popBackStack() },
    )
    settingsGraph(
        navController = navController,
        onBackClick = { navController.popBackStack() },
        onNavigateToStravaConnect = { navController.navigate(STRAVA_CONNECT_ROUTE) },
    )
    stravaConnectScreen(
        onBackClick = { navController.popBackStack() },
    )
}
