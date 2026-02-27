package com.koflox.poi.presentation.buttons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ActivePoiButtonsViewModel(
    private val observeSelectedPoisUseCase: ObserveSelectedPoisUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivePoiButtonsUiState>(ActivePoiButtonsUiState.Loading)
    val uiState: StateFlow<ActivePoiButtonsUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            observeSelectedPoisUseCase.observeSelectedPois().collect { pois ->
                _uiState.value = ActivePoiButtonsUiState.Content(selectedPois = pois)
            }
        }
    }
}
