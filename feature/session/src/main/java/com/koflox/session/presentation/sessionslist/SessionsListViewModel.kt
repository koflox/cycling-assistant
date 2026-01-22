package com.koflox.session.presentation.sessionslist

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.SharePreviewData
import com.koflox.session.presentation.share.ShareResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SessionsListViewModel(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val mapper: SessionsListUiMapper,
    private val sessionUiMapper: SessionUiMapper,
    private val imageSharer: SessionImageSharer,
    private val shareErrorMapper: ShareErrorMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsListUiState())
    val uiState: StateFlow<SessionsListUiState> = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    fun onEvent(event: SessionsListUiEvent) {
        when (event) {
            is SessionsListUiEvent.ShareClicked -> showSharePreview(event.sessionId)
            is SessionsListUiEvent.ShareConfirmed -> shareImage(event.bitmap, event.destinationName)
            SessionsListUiEvent.ShareDialogDismissed -> dismissShareDialog()
            SessionsListUiEvent.ShareIntentLaunched -> clearShareIntent()
            SessionsListUiEvent.ShareErrorDismissed -> clearShareError()
        }
    }

    private fun showSharePreview(sessionId: String) {
        viewModelScope.launch {
            getSessionByIdUseCase.getSession(sessionId)
                .onSuccess { session ->
                    val formattedData = sessionUiMapper.toSessionUiModel(session)
                    val routePoints = session.trackPoints.map { trackPoint ->
                        LatLng(trackPoint.latitude, trackPoint.longitude)
                    }
                    _uiState.update {
                        it.copy(
                            sharePreviewData = SharePreviewData(
                                sessionId = session.id,
                                destinationName = session.destinationName,
                                startDateFormatted = sessionUiMapper.formatStartDate(session.startTimeMs),
                                elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
                                traveledDistanceFormatted = formattedData.traveledDistanceFormatted,
                                averageSpeedFormatted = formattedData.averageSpeedFormatted,
                                topSpeedFormatted = formattedData.topSpeedFormatted,
                                routePoints = routePoints,
                            ),
                        )
                    }
                }
        }
    }

    private fun dismissShareDialog() {
        _uiState.update { it.copy(sharePreviewData = null) }
    }

    private fun shareImage(bitmap: Bitmap, destinationName: String) {
        _uiState.update { it.copy(isSharing = true) }
        viewModelScope.launch {
            val result = imageSharer.shareImage(bitmap, destinationName)
            _uiState.update {
                it.copy(
                    isSharing = false,
                    sharePreviewData = if (result is ShareResult.Success) null else it.sharePreviewData,
                    shareIntent = (result as? ShareResult.Success)?.intent,
                    shareError = shareErrorMapper.map(result),
                )
            }
        }
    }

    private fun clearShareIntent() {
        _uiState.update { it.copy(shareIntent = null) }
    }

    private fun clearShareError() {
        _uiState.update { it.copy(shareError = null) }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            // TODO: apply pagination
            getAllSessionsUseCase.observeAllSessions().collect { sessions ->
                val uiModels = sessions.map { mapper.toUiModel(it) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sessions = uiModels,
                        isEmpty = uiModels.isEmpty(),
                    )
                }
            }
        }
    }
}
