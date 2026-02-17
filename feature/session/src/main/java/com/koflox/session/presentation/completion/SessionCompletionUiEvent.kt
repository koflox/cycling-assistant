package com.koflox.session.presentation.completion

import android.graphics.Bitmap

internal sealed interface SessionCompletionUiEvent {
    data object ShareClicked : SessionCompletionUiEvent
    data class ShareConfirmed(val bitmap: Bitmap, val shareText: String, val chooserTitle: String) : SessionCompletionUiEvent
    data object ShareDialogDismissed : SessionCompletionUiEvent
    data object ShareIntentLaunched : SessionCompletionUiEvent
    data object ErrorDismissed : SessionCompletionUiEvent
}
