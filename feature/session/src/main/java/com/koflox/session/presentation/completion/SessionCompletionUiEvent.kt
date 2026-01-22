package com.koflox.session.presentation.completion

import android.graphics.Bitmap

sealed interface SessionCompletionUiEvent {
    data object ShareClicked : SessionCompletionUiEvent
    data class ShareConfirmed(val bitmap: Bitmap, val destinationName: String) : SessionCompletionUiEvent
    data object ShareDialogDismissed : SessionCompletionUiEvent
    data object ShareIntentLaunched : SessionCompletionUiEvent
    data object ErrorDismissed : SessionCompletionUiEvent
}
