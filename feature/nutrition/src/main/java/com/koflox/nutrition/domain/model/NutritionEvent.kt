package com.koflox.nutrition.domain.model

sealed interface NutritionEvent {
    data class BreakRequired(
        val suggestionTimeMs: Long,
        val intervalNumber: Int,
    ) : NutritionEvent

    data object ChecksStopped : NutritionEvent
}
