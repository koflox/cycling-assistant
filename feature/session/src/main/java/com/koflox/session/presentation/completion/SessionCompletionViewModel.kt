package com.koflox.session.presentation.completion

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.ShareResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class SessionCompletionViewModel(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
    private val imageSharer: SessionImageSharer,
    private val shareErrorMapper: ShareErrorMapper,
    private val dispatcherDefault: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle[SESSION_ID_ARG])

    private val _uiState = MutableStateFlow<SessionCompletionUiState>(SessionCompletionUiState.Loading)
    val uiState: StateFlow<SessionCompletionUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<SessionCompletionNavigation>()
    val navigation = _navigation.receiveAsFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            loadSession()
        }
    }

    fun onEvent(event: SessionCompletionUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                SessionCompletionUiEvent.ShareClicked -> showShareDialog()
                is SessionCompletionUiEvent.ShareConfirmed -> shareImage(event.bitmap, event.destinationName)
                SessionCompletionUiEvent.ShareDialogDismissed -> dismissShareDialog()
                SessionCompletionUiEvent.ShareIntentLaunched -> clearShareIntent()
                SessionCompletionUiEvent.ErrorDismissed -> clearOverlayError()
            }
        }
    }

    private fun showShareDialog() {
        updateContent { it.copy(overlay = Overlay.ShareDialog) }
    }

    private fun dismissShareDialog() {
        updateContent { it.copy(overlay = null) }
    }

    private suspend fun shareImage(bitmap: Bitmap, destinationName: String) {
        updateContent { it.copy(overlay = Overlay.Sharing) }
        val result = imageSharer.shareImage(bitmap, destinationName)
        updateContent { content ->
            when (result) {
                is ShareResult.Success -> content.copy(overlay = Overlay.ShareReady(result.intent))
                else -> {
                    val errorMessage = shareErrorMapper.map(result)
                    content.copy(overlay = if (errorMessage != null) Overlay.ShareError(errorMessage) else Overlay.ShareDialog)
                }
            }
        }
    }

    private fun clearShareIntent() {
        updateContent { it.copy(overlay = null) }
    }

    private fun clearOverlayError() {
        updateContent { content ->
            val newOverlay = if (content.overlay is Overlay.ShareError) Overlay.ShareDialog else content.overlay
            content.copy(overlay = newOverlay)
        }
    }

    private suspend fun loadSession() {
        getSessionByIdUseCase.getSession(sessionId)
            .onSuccess { session ->
                if (session.status != SessionStatus.COMPLETED) {
                    _navigation.send(SessionCompletionNavigation.ToDashboard)
                    return@onSuccess
                }
                val formattedData = sessionUiMapper.toSessionUiModel(session)
                val routePoints = session.trackPoints.map { trackPoint ->
                    LatLng(trackPoint.latitude, trackPoint.longitude)
                }
                _uiState.value = SessionCompletionUiState.Content(
                    sessionId = sessionId,
                    destinationName = session.destinationName,
                    startDateFormatted = sessionUiMapper.formatStartDate(session.startTimeMs),
                    elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
                    traveledDistanceFormatted = formattedData.traveledDistanceFormatted,
                    averageSpeedFormatted = formattedData.averageSpeedFormatted,
                    topSpeedFormatted = formattedData.topSpeedFormatted,
                    altitudeGainFormatted = formattedData.altitudeGainFormatted,
                    routePoints = routePoints,
                )
            }
            .onFailure { error ->
                val message = errorMessageMapper.map(error)
                _uiState.value = SessionCompletionUiState.Error(message)
            }
    }

    private inline fun updateContent(transform: (SessionCompletionUiState.Content) -> SessionCompletionUiState.Content) {
        val current = _uiState.value
        if (current is SessionCompletionUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
