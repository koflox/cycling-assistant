package com.koflox.settings.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.koflox.poisettings.bridge.navigator.PoiSettingsUiNavigator
import com.koflox.settings.presentation.SettingsRoute
import org.koin.compose.koinInject

const val SETTINGS_GRAPH_ROUTE = "settings_graph"

private const val SETTINGS_ROUTE = "settings"
private const val SETTINGS_POI_SELECTION_ROUTE = "settings_poi_selection"

fun NavGraphBuilder.settingsGraph(
    navController: NavController,
    onBackClick: () -> Unit,
) {
    navigation(
        startDestination = SETTINGS_ROUTE,
        route = SETTINGS_GRAPH_ROUTE,
    ) {
        composable(route = SETTINGS_ROUTE) {
            SettingsRoute(
                onBackClick = onBackClick,
                onNavigateToPoiSelection = { navController.navigate(SETTINGS_POI_SELECTION_ROUTE) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable(route = SETTINGS_POI_SELECTION_ROUTE) {
            val poiSettingsUiNavigator: PoiSettingsUiNavigator = koinInject()
            poiSettingsUiNavigator.PoiSelectionScreen(
                onBackClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
