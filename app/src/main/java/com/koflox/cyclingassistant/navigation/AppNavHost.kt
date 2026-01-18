package com.koflox.cyclingassistant.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.koflox.dashboard.navigation.dashboardScreen
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.navigation.sessionCompletionRoute
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.DASHBOARD,
) {
    val sessionUiNavigator: CyclingSessionUiNavigator = koinInject()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        dashboardScreen(
            onNavigateToSessionsList = { navController.navigate(NavRoutes.SESSIONS_LIST) },
            onNavigateToSessionCompletion = { sessionId ->
                navController.navigate(sessionCompletionRoute(sessionId))
            },
        )
        composable(route = NavRoutes.SESSIONS_LIST) {
            sessionUiNavigator.SessionsScreen(
                onBackClick = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(sessionCompletionRoute(sessionId))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable(
            route = NavRoutes.SESSION_COMPLETION,
            arguments = listOf(navArgument(SESSION_ID_ARG) { type = NavType.StringType }),
        ) {
            sessionUiNavigator.SessionCompletionScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.popBackStack(NavRoutes.DASHBOARD, inclusive = false)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
