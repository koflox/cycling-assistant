package com.koflox.destinationnutrition.bridge.model

sealed interface NutritionBreakEvent {
    data class BreakRequired(val suggestionTimeMs: Long) : NutritionBreakEvent
    data object ChecksStopped : NutritionBreakEvent
}
