package com.koflox.cyclingassistant.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.koflox.dashboard.navigation.dashboardScreen
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
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
        )
        composable(route = NavRoutes.SESSIONS_LIST) {
            sessionUiNavigator.SessionsScreen(
                onBackClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
