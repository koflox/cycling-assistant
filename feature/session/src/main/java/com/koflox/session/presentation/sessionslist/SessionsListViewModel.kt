package com.koflox.session.presentation.sessionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SessionsListViewModel @Inject constructor(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val mapper: SessionsListUiMapper,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionsListUiState>(SessionsListUiState.Loading)
    val uiState: StateFlow<SessionsListUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        observeSessions()
    }

    fun onEvent(event: SessionsListUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                SessionsListUiEvent.LoadErrorDismissed -> dismissLoadError()
            }
        }
    }

    private fun dismissLoadError() {
        updateContent { it.copy(overlay = null) }
    }

    private fun observeSessions() {
        viewModelScope.launch(dispatcherDefault) {
            getAllSessionsUseCase.observeAllSessions().collect { sessions ->
                val uiModels = sessions.map { mapper.toUiModel(it) }
                _uiState.value = if (uiModels.isEmpty()) {
                    SessionsListUiState.Empty
                } else {
                    SessionsListUiState.Content(sessions = uiModels)
                }
            }
        }
    }

    private inline fun updateContent(transform: (SessionsListUiState.Content) -> SessionsListUiState.Content) {
        val current = _uiState.value
        if (current is SessionsListUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
