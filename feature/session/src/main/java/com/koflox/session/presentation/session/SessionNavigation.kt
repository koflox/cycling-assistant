package com.koflox.session.presentation.session

internal sealed interface SessionNavigation {
    data class ToCompletion(val sessionId: String) : SessionNavigation
}
