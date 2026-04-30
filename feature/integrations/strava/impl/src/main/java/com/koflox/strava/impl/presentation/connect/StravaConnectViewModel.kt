package com.koflox.strava.impl.presentation.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import com.koflox.strava.impl.oauth.StravaAuthEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StravaConnectViewModel @Inject constructor(
    private val authUseCase: StravaAuthUseCase,
    private val authEvents: StravaAuthEvents,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StravaConnectUiState>(StravaConnectUiState.Loading)
    val uiState: StateFlow<StravaConnectUiState> = _uiState.asStateFlow()

    private val navigationChannel = Channel<StravaConnectNavigation>(Channel.BUFFERED)
    val navigation: Flow<StravaConnectNavigation> = navigationChannel.receiveAsFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            combine(
                authUseCase.observeAuthState(),
                authEvents.hint,
            ) { authState, hint ->
                StravaConnectUiState.Content(authState = authState, hint = hint)
            }.collect { content ->
                val current = _uiState.value
                val nextOverlay = (current as? StravaConnectUiState.Content)?.overlay
                _uiState.value = content.copy(overlay = nextOverlay)
            }
        }
    }

    fun onEvent(event: StravaConnectUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                StravaConnectUiEvent.ConnectClicked -> navigationChannel.send(StravaConnectNavigation.LaunchOAuthIntent)
                StravaConnectUiEvent.LogoutClicked -> showLogoutConfirm()
                StravaConnectUiEvent.LogoutConfirmed -> performLogout()
                StravaConnectUiEvent.LogoutDismissed -> dismissOverlay()
                StravaConnectUiEvent.HintDismissed -> authEvents.consume()
            }
        }
    }

    private fun showLogoutConfirm() {
        updateContent { it.copy(overlay = StravaConnectUiState.Content.Overlay.LogoutConfirm) }
    }

    private fun dismissOverlay() {
        updateContent { it.copy(overlay = null) }
    }

    private suspend fun performLogout() {
        dismissOverlay()
        if (uiState.value.let { it is StravaConnectUiState.Content && it.authState is StravaAuthState.LoggedIn }) {
            authUseCase.logout()
        }
    }

    private fun updateContent(transform: (StravaConnectUiState.Content) -> StravaConnectUiState.Content) {
        val current = _uiState.value
        if (current is StravaConnectUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
