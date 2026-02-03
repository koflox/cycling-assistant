package com.koflox.nutrition.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.nutrition.domain.usecase.ObserveNutritionSettingsUseCase
import com.koflox.nutrition.domain.usecase.UpdateNutritionSettingsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NutritionSettingsViewModel(
    private val observeNutritionSettingsUseCase: ObserveNutritionSettingsUseCase,
    private val updateNutritionSettingsUseCase: UpdateNutritionSettingsUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionSettingsUiState())
    val uiState: StateFlow<NutritionSettingsUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            observeNutritionSettingsUseCase.observeSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        isEnabled = settings.isEnabled,
                        intervalMinutes = settings.intervalMinutes,
                    )
                }
            }
        }
    }

    fun onEvent(event: NutritionSettingsUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is NutritionSettingsUiEvent.EnabledChanged -> handleEnabledChanged(event.isEnabled)
                is NutritionSettingsUiEvent.IntervalChanged -> handleIntervalChanged(event.intervalMinutes)
            }
        }
    }

    private suspend fun handleEnabledChanged(isEnabled: Boolean) {
        updateNutritionSettingsUseCase.setEnabled(isEnabled)
    }

    private suspend fun handleIntervalChanged(intervalMinutes: Int) {
        updateNutritionSettingsUseCase.setIntervalMinutes(intervalMinutes)
    }
}
