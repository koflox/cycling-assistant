package com.koflox.settings.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.koflox.poisettings.bridge.navigator.PoiSettingsUiNavigator
import com.koflox.sessionsettings.bridge.navigator.StatsDisplaySettingsUiNavigator
import com.koflox.settings.presentation.SettingsRoute
import org.koin.compose.koinInject

const val SETTINGS_GRAPH_ROUTE = "settings_graph"

private const val SETTINGS_ROUTE = "settings"
private const val SETTINGS_POI_SELECTION_ROUTE = "settings_poi_selection"
private const val STATS_DISPLAY_BASE_ROUTE = "settings_stats_display"
private const val SECTION_ARG = "section"
private const val SETTINGS_STATS_DISPLAY_ROUTE = "$STATS_DISPLAY_BASE_ROUTE?$SECTION_ARG={$SECTION_ARG}"

fun settingsStatsDisplayRoute(section: String? = null): String =
    if (section != null) "$STATS_DISPLAY_BASE_ROUTE?$SECTION_ARG=$section" else STATS_DISPLAY_BASE_ROUTE

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
                onNavigateToStatsConfig = { navController.navigate(settingsStatsDisplayRoute()) },
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
        composable(
            route = SETTINGS_STATS_DISPLAY_ROUTE,
            arguments = listOf(
                navArgument(SECTION_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val statsDisplaySettingsUiNavigator: StatsDisplaySettingsUiNavigator = koinInject()
            val sectionArg = backStackEntry.arguments?.getString(SECTION_ARG)
            statsDisplaySettingsUiNavigator.StatsDisplayConfigScreen(
                onBackClick = { navController.popBackStack() },
                initialSection = sectionArg,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
