package com.koflox.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.usecase.ObserveSettingsUseCase
import com.koflox.settings.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    companion object {
        private const val MIN_WEIGHT_KG = 1.0
        private const val MAX_WEIGHT_KG = 250.0
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        observeSettings()
        loadRiderWeight()
    }

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is SettingsUiEvent.ThemeSelected -> updateTheme(event.theme)
                is SettingsUiEvent.LanguageSelected -> updateLanguage(event.language)
                is SettingsUiEvent.RiderWeightChanged -> updateRiderWeight(event.input)
                SettingsUiEvent.ThemeDropdownToggled -> toggleThemeDropdown()
                SettingsUiEvent.LanguageDropdownToggled -> toggleLanguageDropdown()
                SettingsUiEvent.DropdownsDismissed -> dismissDropdowns()
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch(dispatcherDefault) {
            combine(
                observeSettingsUseCase.observeTheme(),
                observeSettingsUseCase.observeLanguage(),
            ) { theme, language ->
                Pair(theme, language)
            }.collect { (theme, language) ->
                _uiState.update {
                    it.copy(
                        selectedTheme = theme,
                        selectedLanguage = language,
                    )
                }
            }
        }
    }

    private fun loadRiderWeight() {
        viewModelScope.launch(dispatcherDefault) {
            val weightKg = observeSettingsUseCase.getRiderWeightKg()
            _uiState.update {
                it.copy(riderWeightKg = formatWeight(weightKg))
            }
        }
    }

    private fun formatWeight(weightKg: Float): String {
        return if (weightKg == weightKg.toLong().toFloat()) {
            weightKg.toLong().toString()
        } else {
            weightKg.toString()
        }
    }

    private suspend fun updateTheme(theme: AppTheme) {
        updateSettingsUseCase.updateTheme(theme)
        _uiState.update { it.copy(isThemeDropdownExpanded = false) }
    }

    private suspend fun updateLanguage(language: AppLanguage) {
        updateSettingsUseCase.updateLanguage(language)
        _uiState.update { it.copy(isLanguageDropdownExpanded = false) }
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

    private suspend fun updateRiderWeight(input: String) {
        _uiState.update { it.copy(riderWeightKg = input) }
        val weightKg = input.toDoubleOrNull() ?: return
        if (weightKg in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
            updateSettingsUseCase.updateRiderWeightKg(weightKg)
        }
    }

    private fun dismissDropdowns() {
        _uiState.update {
            it.copy(isThemeDropdownExpanded = false, isLanguageDropdownExpanded = false)
        }
    }
}
