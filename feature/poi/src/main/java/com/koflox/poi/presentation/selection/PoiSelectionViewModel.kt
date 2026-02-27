package com.koflox.poi.presentation.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.poi.domain.model.MAX_SELECTED_POIS
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCase
import com.koflox.poi.domain.usecase.UpdateSelectedPoisUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class PoiSelectionViewModel(
    private val observeSelectedPoisUseCase: ObserveSelectedPoisUseCase,
    private val updateSelectedPoisUseCase: UpdateSelectedPoisUseCase,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PoiSelectionUiState>(PoiSelectionUiState.Loading)
    val uiState: StateFlow<PoiSelectionUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<PoiSelectionNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private val pendingSelection = mutableSetOf<PoiType>()
    private var savedSelection = emptySet<PoiType>()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            val currentSelection = observeSelectedPoisUseCase.observeSelectedPois().first()
            pendingSelection.addAll(currentSelection)
            savedSelection = currentSelection.toSet()
            emitContentState()
        }
    }

    fun onEvent(event: PoiSelectionUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is PoiSelectionUiEvent.PoiToggled -> onPoiToggled(event.type)
                PoiSelectionUiEvent.SaveClicked -> onSaveClicked()
            }
        }
    }

    private fun onPoiToggled(type: PoiType) {
        if (type in pendingSelection) {
            pendingSelection.remove(type)
        } else if (pendingSelection.size < MAX_SELECTED_POIS) {
            pendingSelection.add(type)
        }
        emitContentState()
    }

    private suspend fun onSaveClicked() {
        updateSelectedPoisUseCase.updateSelectedPois(pendingSelection.toList())
        _navigation.send(PoiSelectionNavigation.NavigateBack)
    }

    private fun emitContentState() {
        _uiState.value = PoiSelectionUiState.Content(
            pois = PoiType.entries.map { type ->
                PoiItemUiModel(type = type, isSelected = type in pendingSelection)
            },
            isSaveEnabled = pendingSelection.size == MAX_SELECTED_POIS && pendingSelection != savedSelection,
        )
    }
}
