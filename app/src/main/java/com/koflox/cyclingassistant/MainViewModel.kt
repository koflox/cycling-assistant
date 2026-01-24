package com.koflox.cyclingassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.settings.api.LocaleProvider
import com.koflox.settings.api.ThemeProvider
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class MainViewModel(
    themeProvider: ThemeProvider,
    localeProvider: LocaleProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initialize(themeProvider, localeProvider)
    }

    private fun initialize(themeProvider: ThemeProvider, localeProvider: LocaleProvider) {
        viewModelScope.launch {
            combine(
                themeProvider.observeTheme(),
                localeProvider.observeLanguage(),
            ) { theme, language ->
                MainUiState.Ready(theme = theme, language = language)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

internal sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val theme: AppTheme,
        val language: AppLanguage,
    ) : MainUiState
}
