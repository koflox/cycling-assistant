package com.koflox.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.usecase.ObserveSettingsUseCase
import com.koflox.settings.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ThemeSelected -> updateTheme(event.theme)
            is SettingsUiEvent.LanguageSelected -> updateLanguage(event.language)
            SettingsUiEvent.ThemeDropdownToggled -> toggleThemeDropdown()
            SettingsUiEvent.LanguageDropdownToggled -> toggleLanguageDropdown()
            SettingsUiEvent.DropdownsDismissed -> dismissDropdowns()
            SettingsUiEvent.RestartHintDismissed -> dismissRestartHint()
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                observeSettingsUseCase.observeTheme(),
                observeSettingsUseCase.observeLanguage(),
            ) { theme, language ->
                theme to language
            }.collect { (theme, language) ->
                _uiState.update {
                    it.copy(selectedTheme = theme, selectedLanguage = language)
                }
            }
        }
    }

    private fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            updateSettingsUseCase.updateTheme(theme)
        }
        _uiState.update { it.copy(isThemeDropdownExpanded = false) }
    }

    private fun updateLanguage(language: AppLanguage) {
        val currentLanguage = _uiState.value.selectedLanguage
        viewModelScope.launch {
            updateSettingsUseCase.updateLanguage(language)
        }
        _uiState.update {
            it.copy(
                isLanguageDropdownExpanded = false,
                isLanguageChangeRequiresRestart = language != currentLanguage,
            )
        }
    }

    private fun toggleThemeDropdown() {
        _uiState.update {
            it.copy(
                isThemeDropdownExpanded = !it.isThemeDropdownExpanded,
                isLanguageDropdownExpanded = false,
            )
        }
    }

    private fun toggleLanguageDropdown() {
        _uiState.update {
            it.copy(
                isLanguageDropdownExpanded = !it.isLanguageDropdownExpanded,
                isThemeDropdownExpanded = false,
            )
        }
    }

    private fun dismissDropdowns() {
        _uiState.update {
            it.copy(isThemeDropdownExpanded = false, isLanguageDropdownExpanded = false)
        }
    }

    private fun dismissRestartHint() {
        _uiState.update { it.copy(isLanguageChangeRequiresRestart = false) }
    }
}
