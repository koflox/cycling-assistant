package com.koflox.connections.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.koflox.connections.presentation.listing.DeviceListRoute
import com.koflox.connections.presentation.scanning.BleScanningSheetRoute
import com.koflox.sensor.power.navigation.powerTestModeScreen
import java.net.URLEncoder

const val CONNECTIONS_GRAPH_ROUTE = "connections_graph"

private const val CONNECTIONS_ROUTE = "connections"
private const val CONNECTIONS_SCANNING_ROUTE = "connections_scanning"

private fun powerTestModeRoute(macAddress: String): String =
    "power_test_mode/${URLEncoder.encode(macAddress, "UTF-8")}"

fun NavGraphBuilder.connectionsGraph(
    navController: NavController,
    onBackClick: () -> Unit,
) {
    navigation(
        startDestination = CONNECTIONS_ROUTE,
        route = CONNECTIONS_GRAPH_ROUTE,
    ) {
        composable(route = CONNECTIONS_ROUTE) {
            DeviceListRoute(
                onBackClick = onBackClick,
                onNavigateToTestMode = { macAddress ->
                    navController.navigate(powerTestModeRoute(macAddress))
                },
                onNavigateToScanning = {
                    navController.navigate(CONNECTIONS_SCANNING_ROUTE)
                },
            )
        }
        composable(route = CONNECTIONS_SCANNING_ROUTE) {
            BleScanningSheetRoute(
                onDismiss = { navController.popBackStack() },
            )
        }
        powerTestModeScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
