package com.koflox.settings.presentation

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme

internal data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    // TODO #1: consider the weight as not set and skips stats related on it unless user enters a value,
    // TODO #2: suggest weight selection before the first session recording, UI field should have min and max values validation - 1 and 300 kg
    val riderWeightKg: String = "75",
    val availableThemes: List<AppTheme> = AppTheme.entries,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,
    val isThemeDropdownExpanded: Boolean = false,
    val isLanguageDropdownExpanded: Boolean = false,
)
