package com.koflox.poi.presentation.selection

internal sealed interface PoiSelectionNavigation {
    data object NavigateBack : PoiSelectionNavigation
}
