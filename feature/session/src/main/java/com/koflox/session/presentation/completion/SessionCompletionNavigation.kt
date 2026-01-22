package com.koflox.session.presentation.completion

internal sealed interface SessionCompletionNavigation {
    data object ToDashboard : SessionCompletionNavigation
}
