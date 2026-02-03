package com.koflox.nutrition.presentation.popup

internal sealed interface NutritionPopupUiEvent {
    data object DismissClicked : NutritionPopupUiEvent
}
