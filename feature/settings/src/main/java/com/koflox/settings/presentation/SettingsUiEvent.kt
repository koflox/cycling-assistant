package com.koflox.settings.presentation

import com.koflox.locale.domain.model.AppLanguage
import com.koflox.theme.domain.model.AppTheme

internal sealed interface SettingsUiEvent {
    data class ThemeSelected(val theme: AppTheme) : SettingsUiEvent
    data class LanguageSelected(val language: AppLanguage) : SettingsUiEvent
    data class RiderWeightChanged(val input: String) : SettingsUiEvent
    data object ThemeDropdownToggled : SettingsUiEvent
    data object LanguageDropdownToggled : SettingsUiEvent
    data object DropdownsDismissed : SettingsUiEvent
}
