package com.koflox.session.presentation.sessionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.designsystem.text.UiText
import com.koflox.di.DefaultDispatcher
import com.koflox.session.domain.usecase.DeleteSessionUseCase
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.domain.usecase.RenameSessionUseCase
import com.koflox.session.domain.usecase.SessionNameValidation
import com.koflox.session.domain.usecase.ValidateSessionNameUseCase
import com.koflox.session.history.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
internal class SessionsListViewModel @Inject constructor(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val renameSessionUseCase: RenameSessionUseCase,
    private val validateSessionNameUseCase: ValidateSessionNameUseCase,
    private val mapper: SessionsListUiMapper,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private companion object {
        val VALIDATION_DEBOUNCE = 300.milliseconds
    }

    private val _uiState = MutableStateFlow<SessionsListUiState>(SessionsListUiState.Loading)
    val uiState: StateFlow<SessionsListUiState> = _uiState.asStateFlow()

    private val renameInputs = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        initialize()
    }

    private fun initialize() {
        observeSessions()
        observeRenameInputs()
    }

    @Suppress("CyclomaticComplexMethod")
    fun onEvent(event: SessionsListUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                SessionsListUiEvent.LoadErrorDismissed -> dismissOverlay()
                SessionsListUiEvent.ToastDismissed -> dismissOverlay()
                SessionsListUiEvent.TransientToastShown -> updateContent { it.copy(transientToast = null) }
                SessionsListUiEvent.MenuDismissed -> dismissOverlay()
                SessionsListUiEvent.DeleteDismissed -> dismissOverlay()
                SessionsListUiEvent.RenameDismissed -> dismissOverlay()
                is SessionsListUiEvent.MenuRequested -> showMenu(event.sessionId)
                is SessionsListUiEvent.DeleteRequested -> showDeleteConfirmation(event.sessionId)
                is SessionsListUiEvent.DeleteConfirmed -> deleteSession(event.sessionId)
                is SessionsListUiEvent.RenameRequested -> showRenamePrompt(event.sessionId)
                is SessionsListUiEvent.RenameInputChanged -> handleRenameInputChange(event.input)
                SessionsListUiEvent.RenameConfirmed -> confirmRename()
            }
        }
    }

    private fun showMenu(sessionId: String) {
        val item = currentContent()?.sessions?.find { it.id == sessionId } ?: return
        if (!item.isCompleted) return
        updateContent {
            it.copy(overlay = SessionsListOverlay.Menu(sessionId = sessionId, sessionName = item.displayName))
        }
    }

    private fun showDeleteConfirmation(sessionId: String) {
        updateContent { it.copy(overlay = SessionsListOverlay.DeleteConfirmation(sessionId)) }
    }

    private fun showRenamePrompt(sessionId: String) {
        val item = currentContent()?.sessions?.find { it.id == sessionId } ?: return
        val current = item.displayName
        updateContent {
            it.copy(
                overlay = SessionsListOverlay.RenamePrompt(
                    sessionId = sessionId,
                    currentName = current,
                    input = current,
                    lastValidatedInput = current,
                    validation = SessionNameValidation.SameAsCurrent,
                ),
            )
        }
    }

    private fun handleRenameInputChange(input: String) {
        val prompt = currentRenamePrompt() ?: return
        val sanitized = input.filter { it.isLetterOrDigit() || it == ' ' }
        if (prompt.input == sanitized) return
        updateContent { it.copy(overlay = prompt.copy(input = sanitized)) }
        renameInputs.tryEmit(sanitized)
    }

    @OptIn(FlowPreview::class)
    private fun observeRenameInputs() {
        viewModelScope.launch(dispatcherDefault) {
            renameInputs
                .debounce(VALIDATION_DEBOUNCE)
                .collect { input ->
                    val prompt = currentRenamePrompt() ?: return@collect
                    if (prompt.input != input) return@collect
                    val validation = validateSessionNameUseCase.validate(input, prompt.currentName)
                    val latest = currentRenamePrompt() ?: return@collect
                    if (latest.input != input) return@collect
                    updateContent {
                        it.copy(overlay = latest.copy(lastValidatedInput = input, validation = validation))
                    }
                }
        }
    }

    private suspend fun confirmRename() {
        val prompt = currentRenamePrompt() ?: return
        if (!prompt.isSaveEnabled) return
        val result = renameSessionUseCase.rename(prompt.sessionId, prompt.input)
        if (result.isSuccess) {
            updateContent {
                it.copy(overlay = SessionsListOverlay.Toast(UiText.Resource(R.string.sessions_list_rename_success)))
            }
        } else {
            updateContent { it.copy(transientToast = UiText.Resource(R.string.sessions_list_rename_error)) }
        }
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
                    val carriedToast = (current as? SessionsListUiState.Content)?.transientToast
                    SessionsListUiState.Content(
                        sessions = uiModels,
                        overlay = carriedOverlay,
                        transientToast = carriedToast,
                    )
                }
            }
        }
    }

    private fun currentContent(): SessionsListUiState.Content? = _uiState.value as? SessionsListUiState.Content
    private fun currentRenamePrompt(): SessionsListOverlay.RenamePrompt? =
        currentContent()?.overlay as? SessionsListOverlay.RenamePrompt

    private inline fun updateContent(transform: (SessionsListUiState.Content) -> SessionsListUiState.Content) {
        val current = _uiState.value
        if (current is SessionsListUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
