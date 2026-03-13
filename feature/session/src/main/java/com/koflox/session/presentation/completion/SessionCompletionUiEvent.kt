package com.koflox.session.presentation.completion

import com.koflox.session.presentation.route.MapLayer

internal sealed interface SessionCompletionUiEvent {
    data class LayerSelected(val layer: MapLayer) : SessionCompletionUiEvent
}
