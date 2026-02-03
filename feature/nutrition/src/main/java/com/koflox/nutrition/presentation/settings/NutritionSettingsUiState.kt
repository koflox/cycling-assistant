package com.koflox.nutrition.presentation.settings

import com.koflox.nutrition.domain.model.NutritionSettings

internal data class NutritionSettingsUiState(
    val isEnabled: Boolean = NutritionSettings.DEFAULT_ENABLED,
    val intervalMinutes: Int = NutritionSettings.DEFAULT_INTERVAL_MINUTES,
    val minIntervalMinutes: Int = NutritionSettings.MIN_INTERVAL_MINUTES,
    val maxIntervalMinutes: Int = NutritionSettings.MAX_INTERVAL_MINUTES,
    val intervalStepMinutes: Int = NutritionSettings.INTERVAL_STEP_MINUTES,
)
