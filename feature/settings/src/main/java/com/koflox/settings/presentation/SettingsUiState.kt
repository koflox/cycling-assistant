package com.koflox.settings.presentation

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme

internal data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    // TODO:
    //  1. suggest weight selection before the first session recording
    //  2. UI field should have min and max values validation - 1 and 300 kg
    //  3. UI field hint should explain that weight is used for calories calculation, when no weight is set
    val riderWeightKg: String = "",
    val availableThemes: List<AppTheme> = AppTheme.entries,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,
    val isThemeDropdownExpanded: Boolean = false,
    val isLanguageDropdownExpanded: Boolean = false,
)
