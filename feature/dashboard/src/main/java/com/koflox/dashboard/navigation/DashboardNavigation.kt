package com.koflox.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.koflox.dashboard.presentation.DashboardScreen

const val DASHBOARD_ROUTE = "dashboard"

fun NavGraphBuilder.dashboardScreen(
    onNavigateToSessionsList: () -> Unit,
) {
    composable(route = DASHBOARD_ROUTE) {
        DashboardScreen(
            onNavigateToSessionsList = onNavigateToSessionsList,
        )
    }
}
