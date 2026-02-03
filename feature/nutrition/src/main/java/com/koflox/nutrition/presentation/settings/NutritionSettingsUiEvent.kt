package com.koflox.nutrition.presentation.settings

internal sealed interface NutritionSettingsUiEvent {
    data class EnabledChanged(val isEnabled: Boolean) : NutritionSettingsUiEvent
    data class IntervalChanged(val intervalMinutes: Int) : NutritionSettingsUiEvent
}
