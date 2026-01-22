package com.koflox.session.presentation.sessionslist

import android.graphics.Bitmap

internal sealed interface SessionsListUiEvent {
    data class ShareClicked(val sessionId: String) : SessionsListUiEvent
    data class ShareConfirmed(val bitmap: Bitmap, val destinationName: String) : SessionsListUiEvent
    data object ShareDialogDismissed : SessionsListUiEvent
    data object ShareIntentLaunched : SessionsListUiEvent
    data object ShareErrorDismissed : SessionsListUiEvent
}
