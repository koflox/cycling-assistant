package com.koflox.poi.presentation.selection

import com.koflox.poi.domain.model.PoiType

internal sealed interface PoiSelectionUiState {
    data object Loading : PoiSelectionUiState
    data class Content(
        val selectedPois: List<PoiItemUiModel>,
        val availablePois: List<PoiItemUiModel>,
        val isAddEnabled: Boolean,
        val isSaveEnabled: Boolean,
    ) : PoiSelectionUiState
}

internal data class PoiItemUiModel(
    val type: PoiType,
)
