package com.koflox.session.presentation.sessionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionsListViewModel(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val mapper: SessionsListUiMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsListUiState())
    val uiState: StateFlow<SessionsListUiState> = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            // TODO: apply pagination
            getAllSessionsUseCase.observeAllSessions().collect { sessions ->
                val uiModels = sessions.map { mapper.toUiModel(it) }
                _uiState.update {
                    it.copy(
                        sessions = uiModels,
                        isEmpty = uiModels.isEmpty(),
                    )
                }
            }
        }
    }
}
