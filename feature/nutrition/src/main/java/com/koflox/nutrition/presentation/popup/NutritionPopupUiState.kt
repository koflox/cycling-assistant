package com.koflox.nutrition.presentation.popup

internal sealed interface NutritionPopupUiState {
    data object Hidden : NutritionPopupUiState

    data class Visible(
        val elapsedTimerFormatted: String,
    ) : NutritionPopupUiState
}
