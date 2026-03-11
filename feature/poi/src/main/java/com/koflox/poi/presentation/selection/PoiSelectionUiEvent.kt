package com.koflox.poi.presentation.selection

import com.koflox.poi.domain.model.PoiType

internal sealed interface PoiSelectionUiEvent {
    data class PoiAdded(val type: PoiType) : PoiSelectionUiEvent
    data class PoiRemoved(val type: PoiType) : PoiSelectionUiEvent
    data class PoiReordered(val fromIndex: Int, val toIndex: Int) : PoiSelectionUiEvent
    data object SaveClicked : PoiSelectionUiEvent
}
