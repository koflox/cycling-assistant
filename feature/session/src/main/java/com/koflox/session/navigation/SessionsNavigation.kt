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

const val SESSION_ID_ARG = "sessionId"
const val SESSION_COMPLETION_ROUTE = "session_completion/{$SESSION_ID_ARG}"

fun sessionCompletionRoute(sessionId: String): String = "session_completion/$sessionId"

fun NavGraphBuilder.sessionsListScreen(
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
) {
    composable(route = SESSIONS_LIST_ROUTE) {
        SessionsListRoute(
            onBackClick = onBackClick,
            onSessionClick = onSessionClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

fun NavGraphBuilder.sessionCompletionScreen(
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit,
) {
    composable(
        route = SESSION_COMPLETION_ROUTE,
        arguments = listOf(navArgument(SESSION_ID_ARG) { type = NavType.StringType }),
    ) {
        SessionCompletionRoute(
            onBackClick = onBackClick,
            onNavigateToDashboard = onNavigateToDashboard,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
