package com.koflox.session.presentation.share

import android.graphics.Bitmap

internal sealed interface ShareUiEvent {
    data class TabSelected(val tab: ShareTab) : ShareUiEvent

    sealed interface Image : ShareUiEvent {
        data class ShareConfirmed(val bitmap: Bitmap, val shareText: String, val chooserTitle: String) : Image
        data object IntentLaunched : Image
        data object ErrorDismissed : Image
    }

    sealed interface Gpx : ShareUiEvent {
        data object ShareClicked : Gpx
        data object IntentLaunched : Gpx
        data object ErrorDismissed : Gpx
    }
}
