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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SessionCompletionViewModel(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
    private val imageSharer: SessionImageSharer,
    private val shareErrorMapper: ShareErrorMapper,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle[SESSION_ID_ARG])

    private val _uiState = MutableStateFlow(
        SessionCompletionUiState(
            sessionId = sessionId,
        )
    )
    val uiState: StateFlow<SessionCompletionUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    fun onEvent(event: SessionCompletionUiEvent) {
        when (event) {
            SessionCompletionUiEvent.ShareClicked -> showShareDialog()
            is SessionCompletionUiEvent.ShareConfirmed -> shareImage(event.bitmap, event.destinationName)
            SessionCompletionUiEvent.ShareDialogDismissed -> dismissShareDialog()
            SessionCompletionUiEvent.ShareIntentLaunched -> clearShareIntent()
            SessionCompletionUiEvent.ErrorDismissed -> clearError()
        }
    }

    private fun showShareDialog() {
        _uiState.update { it.copy(showShareDialog = true) }
    }

    private fun dismissShareDialog() {
        _uiState.update { it.copy(showShareDialog = false) }
    }

    private fun shareImage(bitmap: Bitmap, destinationName: String) {
        _uiState.update { it.copy(isSharing = true) }
        viewModelScope.launch {
            val result = imageSharer.shareImage(bitmap, destinationName)
            _uiState.update {
                it.copy(
                    isSharing = false,
                    showShareDialog = result !is ShareResult.Success,
                    shareIntent = (result as? ShareResult.Success)?.intent,
                    error = shareErrorMapper.map(result),
                )
            }
        }
    }

    private fun clearShareIntent() {
        _uiState.update { it.copy(shareIntent = null) }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadSession() {
        viewModelScope.launch {
            getSessionByIdUseCase.getSession(sessionId)
                .onSuccess { session ->
                    if (session.status != SessionStatus.COMPLETED) {
                        _uiState.update {
                            it.copy(isLoading = false, shouldNavigateToDashboard = true)
                        }
                        return@onSuccess
                    }
                    val formattedData = sessionUiMapper.toSessionUiModel(session)
                    val routePoints = session.trackPoints.map { trackPoint ->
                        LatLng(trackPoint.latitude, trackPoint.longitude)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sessionId = sessionId,
                            destinationName = session.destinationName,
                            startDateFormatted = sessionUiMapper.formatStartDate(session.startTimeMs),
                            elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
                            traveledDistanceFormatted = formattedData.traveledDistanceFormatted,
                            averageSpeedFormatted = formattedData.averageSpeedFormatted,
                            topSpeedFormatted = formattedData.topSpeedFormatted,
                            routePoints = routePoints,
                        )
                    }
                }
                .onFailure { error ->
                    showSessionError(error)
                }
        }
    }

    private fun showSessionError(error: Throwable) {
        viewModelScope.launch {
            val message = errorMessageMapper.map(error)
            _uiState.update {
                it.copy(isLoading = false, error = message)
            }
        }
    }
}
