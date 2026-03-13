package com.koflox.session.presentation.share

import android.content.Intent
import com.koflox.designsystem.text.UiText

internal enum class ShareTab {
    IMAGE,
    GPX,
}

internal sealed interface ShareUiState {
    data object Loading : ShareUiState

    data class Content(
        val sharePreviewData: SharePreviewData,
        val selectedTab: ShareTab = ShareTab.IMAGE,
        val imageShareState: ImageShareState = ImageShareState.Idle,
        val gpxShareState: GpxShareState,
    ) : ShareUiState
}

internal sealed interface ImageShareState {
    data object Idle : ImageShareState
    data object Sharing : ImageShareState
    data class Ready(val intent: Intent) : ImageShareState
    data class Error(val message: UiText) : ImageShareState
}

internal sealed interface GpxShareState {
    data object Idle : GpxShareState
    data object Generating : GpxShareState
    data class Ready(val intent: Intent) : GpxShareState
    data class Error(val message: UiText) : GpxShareState
    data object Unavailable : GpxShareState
}
