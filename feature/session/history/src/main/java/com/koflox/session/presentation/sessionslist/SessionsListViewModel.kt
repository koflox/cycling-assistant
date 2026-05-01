package com.koflox.session.presentation.sessionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.designsystem.text.UiText
import com.koflox.di.DefaultDispatcher
import com.koflox.session.domain.usecase.DeleteSessionUseCase
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.history.R
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
    private val deleteSessionUseCase: DeleteSessionUseCase,
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
                SessionsListUiEvent.LoadErrorDismissed -> dismissOverlay()
                SessionsListUiEvent.ToastDismissed -> dismissOverlay()
                SessionsListUiEvent.DeleteDismissed -> dismissOverlay()
                is SessionsListUiEvent.DeleteRequested -> showDeleteConfirmation(event.sessionId)
                is SessionsListUiEvent.DeleteConfirmed -> deleteSession(event.sessionId)
            }
        }
    }

    private fun showDeleteConfirmation(sessionId: String) {
        updateContent { it.copy(overlay = SessionsListOverlay.DeleteConfirmation(sessionId)) }
    }

    private suspend fun deleteSession(sessionId: String) {
        val result = deleteSessionUseCase.delete(sessionId)
        val message = if (result.isSuccess) {
            UiText.Resource(R.string.sessions_list_delete_success)
        } else {
            UiText.Resource(R.string.sessions_list_delete_error)
        }
        updateContent { it.copy(overlay = SessionsListOverlay.Toast(message)) }
    }

    private fun dismissOverlay() {
        updateContent { it.copy(overlay = null) }
    }

    private fun observeSessions() {
        viewModelScope.launch(dispatcherDefault) {
            getAllSessionsUseCase.observeAllSessions().collect { sessions ->
                val uiModels = sessions.map { mapper.toUiModel(it) }
                val current = _uiState.value
                _uiState.value = if (uiModels.isEmpty()) {
                    SessionsListUiState.Empty
                } else {
                    val carriedOverlay = (current as? SessionsListUiState.Content)?.overlay
                    SessionsListUiState.Content(sessions = uiModels, overlay = carriedOverlay)
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
