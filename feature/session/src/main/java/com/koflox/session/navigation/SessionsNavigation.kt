package com.koflox.session.navigation

const val SESSIONS_LIST_ROUTE = "sessions_list"

const val SESSION_ID_ARG = "sessionId"
const val SESSION_COMPLETION_ROUTE = "session_completion/{$SESSION_ID_ARG}"

fun sessionCompletionRoute(sessionId: String): String = "session_completion/$sessionId"
