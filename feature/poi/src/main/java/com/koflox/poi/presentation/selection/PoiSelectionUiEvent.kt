package com.koflox.poi.presentation.selection

import com.koflox.poi.domain.model.PoiType

internal sealed interface PoiSelectionUiEvent {
    data class PoiToggled(val type: PoiType) : PoiSelectionUiEvent
    data object SaveClicked : PoiSelectionUiEvent
}
