package com.koflox.poi.presentation.selection

import com.koflox.poi.domain.model.PoiType

internal sealed interface PoiSelectionUiState {
    data object Loading : PoiSelectionUiState
    data class Content(
        val pois: List<PoiItemUiModel>,
        val isSaveEnabled: Boolean,
    ) : PoiSelectionUiState
}

internal data class PoiItemUiModel(
    val type: PoiType,
    val isSelected: Boolean,
)
