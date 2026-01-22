package com.koflox.settings.presentation

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme

internal data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val availableThemes: List<AppTheme> = AppTheme.entries,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,
    val isThemeDropdownExpanded: Boolean = false,
    val isLanguageDropdownExpanded: Boolean = false,
    val isLanguageChangeRequiresRestart: Boolean = false,
)
