package com.koflox.poi.presentation.buttons

internal sealed interface ActivePoiButtonsUiEvent {
    data object MoreClicked : ActivePoiButtonsUiEvent
    data object MoreDialogDismissed : ActivePoiButtonsUiEvent
}
