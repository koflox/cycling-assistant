package com.koflox.poi.presentation.buttons

import com.koflox.poi.domain.model.PoiType

internal sealed interface ActivePoiButtonsUiState {
    data object Loading : ActivePoiButtonsUiState
    data class Content(val selectedPois: List<PoiType>) : ActivePoiButtonsUiState
}
