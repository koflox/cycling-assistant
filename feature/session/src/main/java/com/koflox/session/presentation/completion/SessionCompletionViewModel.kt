package com.koflox.session.presentation.completion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionCompletionViewModel(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle[SESSION_ID_ARG])

    private val _uiState = MutableStateFlow(SessionCompletionUiState())
    val uiState: StateFlow<SessionCompletionUiState> = _uiState.asStateFlow()

    init {
        loadSession()
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
                    showError(error)
                }
        }
    }

    private fun showError(error: Throwable) {
        viewModelScope.launch {
            val message = errorMessageMapper.map(error)
            _uiState.update {
                it.copy(isLoading = false, error = message)
            }
        }
    }
}
