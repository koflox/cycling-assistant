package com.koflox.session.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.koflox.session.presentation.completion.SessionCompletionRoute
import com.koflox.session.presentation.sessionslist.SessionsListRoute

const val SESSIONS_LIST_ROUTE = "sessions_list"

internal const val SESSION_ID_ARG = "sessionId"
internal const val SESSION_COMPLETION_ROUTE = "session_completion/{$SESSION_ID_ARG}"

internal const val STATS_SECTION_COMPLETED = "COMPLETED_SESSION"
internal const val STATS_SECTION_SHARE = "SHARE"

fun sessionCompletionRoute(sessionId: String): String = "session_completion/$sessionId"

fun NavGraphBuilder.sessionsListScreen(
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
) {
    composable(route = SESSIONS_LIST_ROUTE) {
        SessionsListRoute(
            onBackClick = onBackClick,
            onSessionClick = onSessionClick,
            onNavigateToStatsConfig = onNavigateToStatsConfig,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

fun NavGraphBuilder.sessionCompletionScreen(
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
) {
    composable(
        route = SESSION_COMPLETION_ROUTE,
        arguments = listOf(navArgument(SESSION_ID_ARG) { type = NavType.StringType }),
    ) {
        SessionCompletionRoute(
            onBackClick = onBackClick,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToStatsConfig = onNavigateToStatsConfig,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
