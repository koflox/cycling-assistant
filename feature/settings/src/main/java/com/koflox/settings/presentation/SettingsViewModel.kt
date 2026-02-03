package com.koflox.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.locale.domain.usecase.UpdateLocaleUseCase
import com.koflox.profile.domain.model.InvalidWeightException
import com.koflox.profile.domain.usecase.GetRiderWeightUseCase
import com.koflox.profile.domain.usecase.UpdateRiderWeightUseCase
import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import com.koflox.theme.domain.usecase.UpdateThemeUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val observeThemeUseCase: ObserveThemeUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val observeLocaleUseCase: ObserveLocaleUseCase,
    private val updateLocaleUseCase: UpdateLocaleUseCase,
    private val getRiderWeightUseCase: GetRiderWeightUseCase,
    private val updateRiderWeightUseCase: UpdateRiderWeightUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    companion object {
        private const val WEIGHT_INPUT_DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var weightUpdateJob: Job? = null

    init {
        initialize()
    }

    private fun initialize() {
        observeSettings()
        loadRiderWeight()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.RiderWeightChanged -> scheduleWeightUpdate(event.input)
            else -> viewModelScope.launch(dispatcherDefault) {
                when (event) {
                    is SettingsUiEvent.ThemeSelected -> updateTheme(event.theme)
                    is SettingsUiEvent.LanguageSelected -> updateLanguage(event.language)
                    SettingsUiEvent.ThemeDropdownToggled -> toggleThemeDropdown()
                    SettingsUiEvent.LanguageDropdownToggled -> toggleLanguageDropdown()
                    SettingsUiEvent.DropdownsDismissed -> dismissDropdowns()
                    is SettingsUiEvent.RiderWeightChanged -> Unit
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch(dispatcherDefault) {
            combine(
                observeThemeUseCase.observeTheme(),
                observeLocaleUseCase.observeLanguage(),
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
            val weightKg = getRiderWeightUseCase.getRiderWeightKg()
            _uiState.update {
                it.copy(riderWeightKg = formatWeight(weightKg))
            }
        }
    }

    private fun formatWeight(weightKg: Float?): String {
        if (weightKg == null) return ""
        return if (weightKg == weightKg.toLong().toFloat()) {
            weightKg.toLong().toString()
        } else {
            weightKg.toString()
        }
    }

    private suspend fun updateTheme(theme: AppTheme) {
        updateThemeUseCase.updateTheme(theme)
        _uiState.update { it.copy(isThemeDropdownExpanded = false) }
    }

    private suspend fun updateLanguage(language: AppLanguage) {
        updateLocaleUseCase.updateLanguage(language)
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

    private fun scheduleWeightUpdate(input: String) {
        _uiState.update { it.copy(riderWeightKg = input) }
        weightUpdateJob?.cancel()
        weightUpdateJob = viewModelScope.launch(dispatcherDefault) {
            delay(WEIGHT_INPUT_DEBOUNCE_MS)
            updateRiderWeightUseCase.updateRiderWeightKg(input)
                .onSuccess {
                    _uiState.update { it.copy(riderWeightError = null) }
                }
                .onFailure { error ->
                    val weightError = (error as? InvalidWeightException)?.let {
                        RiderWeightError(
                            minWeightKg = it.minWeightKg.toInt(),
                            maxWeightKg = it.maxWeightKg.toInt(),
                        )
                    }
                    _uiState.update { it.copy(riderWeightError = weightError) }
                }
        }
    }

    private fun dismissDropdowns() {
        _uiState.update {
            it.copy(isThemeDropdownExpanded = false, isLanguageDropdownExpanded = false)
        }
    }
}
