package com.koflox.nutrition.domain.model

data class NutritionSettings(
    val isEnabled: Boolean,
    val intervalMinutes: Int,
) {
    companion object {
        const val DEFAULT_ENABLED = true
        const val DEFAULT_INTERVAL_MINUTES = 25
        const val MIN_INTERVAL_MINUTES = 5
        const val MAX_INTERVAL_MINUTES = 60
        const val INTERVAL_STEP_MINUTES = 5
    }
}
