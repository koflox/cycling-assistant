package com.koflox.cyclingassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.session.service.PendingSessionAction
import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class MainViewModel(
    observeThemeUseCase: ObserveThemeUseCase,
    observeLocaleUseCase: ObserveLocaleUseCase,
    private val pendingSessionAction: PendingSessionAction,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initialize(observeThemeUseCase, observeLocaleUseCase)
    }

    fun handleIntent(action: String?) {
        action?.let(pendingSessionAction::handleIntentAction)
    }

    private fun initialize(observeThemeUseCase: ObserveThemeUseCase, observeLocaleUseCase: ObserveLocaleUseCase) {
        viewModelScope.launch {
            combine(
                observeThemeUseCase.observeTheme(),
                observeLocaleUseCase.observeLanguage(),
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
