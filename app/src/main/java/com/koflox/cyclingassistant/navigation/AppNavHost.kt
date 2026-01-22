package com.koflox.cyclingassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.koflox.dashboard.navigation.DASHBOARD_ROUTE
import com.koflox.dashboard.navigation.dashboardScreen
import com.koflox.session.navigation.SESSIONS_LIST_ROUTE
import com.koflox.session.navigation.sessionCompletionRoute
import com.koflox.session.navigation.sessionCompletionScreen
import com.koflox.session.navigation.sessionsListScreen
import com.koflox.settings.navigation.SETTINGS_ROUTE
import com.koflox.settings.navigation.settingsScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DASHBOARD_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        dashboardScreen(
            onNavigateToSessionsList = { navController.navigate(SESSIONS_LIST_ROUTE) },
            onNavigateToSettings = { navController.navigate(SETTINGS_ROUTE) },
            onNavigateToSessionCompletion = { sessionId ->
                navController.navigate(sessionCompletionRoute(sessionId))
            },
        )
        sessionsListScreen(
            onBackClick = { navController.popBackStack() },
            onSessionClick = { sessionId ->
                navController.navigate(sessionCompletionRoute(sessionId))
            },
        )
        sessionCompletionScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToDashboard = {
                navController.popBackStack(DASHBOARD_ROUTE, inclusive = false)
            },
        )
        settingsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
