package com.koflox.settings.presentation

import com.koflox.locale.domain.model.AppLanguage
import com.koflox.theme.domain.model.AppTheme

internal data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val riderWeightKg: String = "",
    val riderWeightError: RiderWeightError? = null,
    val availableThemes: List<AppTheme> = AppTheme.entries,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,
    val isThemeDropdownExpanded: Boolean = false,
    val isLanguageDropdownExpanded: Boolean = false,
) {
    val isRiderWeightError: Boolean get() = riderWeightError != null
}

internal data class RiderWeightError(
    val minWeightKg: Int,
    val maxWeightKg: Int,
)
