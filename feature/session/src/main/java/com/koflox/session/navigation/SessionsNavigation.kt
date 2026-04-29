package com.koflox.session.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.koflox.session.presentation.completion.SessionCompletionRoute
import com.koflox.session.presentation.sessionslist.SessionsListRoute
import com.koflox.session.presentation.share.ShareSessionRoute
import com.koflox.session.presentation.statsdisplay.StatsDisplayConfigRoute
import com.koflox.session.presentation.statsdisplay.StatsDisplaySection

const val SESSIONS_LIST_ROUTE = "sessions_list"
const val SESSIONS_GRAPH_ROUTE = "sessions_graph"

internal const val SESSION_ID_ARG = "sessionId"
internal const val SESSION_COMPLETION_ROUTE = "session_completion/{$SESSION_ID_ARG}"

internal const val STATS_SECTION_COMPLETED = "COMPLETED_SESSION"
internal const val STATS_SECTION_SHARE = "SHARE"

private const val SHARE_SESSION_ROUTE = "share_session/{$SESSION_ID_ARG}"
private const val STATS_CONFIG_BASE_ROUTE = "session_stats_config"
private const val SECTION_ARG = "section"
private const val STATS_CONFIG_ROUTE = "$STATS_CONFIG_BASE_ROUTE?$SECTION_ARG={$SECTION_ARG}"

fun sessionCompletionRoute(sessionId: String): String = "session_completion/$sessionId"

private fun shareSessionRoute(sessionId: String): String = "share_session/$sessionId"

private fun statsConfigRoute(section: String): String = "$STATS_CONFIG_BASE_ROUTE?$SECTION_ARG=$section"

fun NavGraphBuilder.sessionGraph(
    navController: NavController,
    onNavigateToDashboard: () -> Unit,
    onBackFromList: () -> Unit,
) {
    navigation(
        startDestination = SESSIONS_LIST_ROUTE,
        route = SESSIONS_GRAPH_ROUTE,
    ) {
        sessionsListScreen(navController, onBackFromList)
        sessionCompletionScreen(navController, onNavigateToDashboard)
        shareSessionDialog(navController)
        statsConfigScreen(navController)
    }
}

private fun NavGraphBuilder.sessionsListScreen(
    navController: NavController,
    onBackClick: () -> Unit,
) {
    composable(route = SESSIONS_LIST_ROUTE) {
        SessionsListRoute(
            onBackClick = onBackClick,
            onSessionClick = { sessionId -> navController.navigate(sessionCompletionRoute(sessionId)) },
            onShareClick = { sessionId -> navController.navigate(shareSessionRoute(sessionId)) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun NavGraphBuilder.sessionCompletionScreen(
    navController: NavController,
    onNavigateToDashboard: () -> Unit,
) {
    composable(
        route = SESSION_COMPLETION_ROUTE,
        arguments = listOf(navArgument(SESSION_ID_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString(SESSION_ID_ARG) ?: return@composable
        SessionCompletionRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToDashboard = onNavigateToDashboard,
            onShareClick = { navController.navigate(shareSessionRoute(sessionId)) },
            onNavigateToStatsConfig = { section -> navController.navigate(statsConfigRoute(section)) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun NavGraphBuilder.shareSessionDialog(navController: NavController) {
    dialog(
        route = SHARE_SESSION_ROUTE,
        arguments = listOf(navArgument(SESSION_ID_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString(SESSION_ID_ARG) ?: return@dialog
        ShareSessionRoute(
            sessionId = sessionId,
            onDismiss = { navController.popBackStack() },
            onNavigateToStatsConfig = { section -> navController.navigate(statsConfigRoute(section)) },
        )
    }
}

private fun NavGraphBuilder.statsConfigScreen(navController: NavController) {
    composable(
        route = STATS_CONFIG_ROUTE,
        arguments = listOf(
            navArgument(SECTION_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val sectionArg = backStackEntry.arguments?.getString(SECTION_ARG)
        val section = sectionArg?.let { arg -> StatsDisplaySection.entries.find { it.name == arg } }
        StatsDisplayConfigRoute(
            onBackClick = { navController.popBackStack() },
            initialSection = section,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
