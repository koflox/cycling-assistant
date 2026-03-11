package com.koflox.session.presentation.sessionslist

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.di.SessionErrorMapper
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.bearing.calculateBearingDegrees
import com.koflox.location.model.Location
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.route.buildRouteDisplayData
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.SharePreviewData
import com.koflox.session.presentation.share.ShareResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SessionsListViewModel @Inject constructor(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase,
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase,
    private val mapper: SessionsListUiMapper,
    private val sessionUiMapper: SessionUiMapper,
    private val imageSharer: SessionImageSharer,
    @param:SessionErrorMapper private val errorMessageMapper: ErrorMessageMapper,
    private val shareErrorMapper: ShareErrorMapper,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    companion object {
        private const val JOULES_PER_KCAL = 1000.0
    }

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
                is SessionsListUiEvent.ShareClicked -> showSharePreview(event.sessionId)
                is SessionsListUiEvent.ShareConfirmed -> shareImage(event.bitmap, event.shareText, event.chooserTitle)
                SessionsListUiEvent.ShareDialogDismissed -> dismissShareDialog()
                SessionsListUiEvent.ShareIntentLaunched -> dismissShareDialog()
                SessionsListUiEvent.ShareErrorDismissed -> clearShareError()
                SessionsListUiEvent.LoadErrorDismissed -> dismissLoadError()
            }
        }
    }

    private suspend fun showSharePreview(sessionId: String) {
        try {
            coroutineScope {
                val sessionDeferred = async { getSessionByIdUseCase.getSession(sessionId) }
                val statsDeferred = async { calculateSessionStatsUseCase.calculate(sessionId) }
                val sessionResult = sessionDeferred.await()
                val statsResult = statsDeferred.await()
                val session = sessionResult.getOrThrow()
                val derivedStats = statsResult.getOrThrow()
                val formattedData = sessionUiMapper.toSessionUiModel(session)
                val routeDisplayData = buildRouteDisplayData(session.trackPoints)
                val allPoints = routeDisplayData.allPoints
                val endRotation = if (allPoints.size >= 2) {
                    calculateBearingDegrees(
                        from = Location(allPoints[allPoints.lastIndex - 1].latitude, allPoints[allPoints.lastIndex - 1].longitude),
                        to = Location(allPoints.last().latitude, allPoints.last().longitude),
                    )
                } else {
                    0f
                }
                val caloriesFormatted = if (session.hasPowerData && session.totalEnergyJoules != null) {
                    sessionUiMapper.formatCalories(session.totalEnergyJoules / JOULES_PER_KCAL)
                } else {
                    derivedStats.caloriesBurned?.let(sessionUiMapper::formatCalories)
                }
                val shareConfig = observeStatsDisplayConfigUseCase.observeShareStats().first()
                val shareStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, shareConfig)
                val previewData = SharePreviewData(
                    sessionId = session.id,
                    destinationName = session.destinationName,
                    startDateFormatted = sessionUiMapper.formatStartDate(session.startTimeMs),
                    elapsedTimeFormatted = formattedData.elapsedTimeFormatted,
                    movingTimeFormatted = sessionUiMapper.formatElapsedTime(derivedStats.movingTimeMs),
                    idleTimeFormatted = sessionUiMapper.formatElapsedTime(derivedStats.idleTimeMs),
                    traveledDistanceFormatted = formattedData.traveledDistanceFormatted,
                    averageSpeedFormatted = formattedData.averageSpeedFormatted,
                    topSpeedFormatted = formattedData.topSpeedFormatted,
                    altitudeGainFormatted = formattedData.altitudeGainFormatted,
                    altitudeLossFormatted = sessionUiMapper.formatAltitudeGain(derivedStats.altitudeLossMeters),
                    caloriesFormatted = caloriesFormatted,
                    averagePowerFormatted = session.averagePowerWatts?.let(sessionUiMapper::formatPower),
                    maxPowerFormatted = session.maxPowerWatts?.let(sessionUiMapper::formatPower),
                    shareStats = shareStats,
                    routeDisplayData = routeDisplayData,
                    endMarkerRotation = endRotation,
                )
                updateContent { it.copy(overlay = SessionsListOverlay.SharePreview(previewData)) }
            }
        } catch (e: Exception) {
            val errorMessage = errorMessageMapper.map(e)
            updateContent { it.copy(overlay = SessionsListOverlay.LoadError(errorMessage)) }
        }
    }

    private fun dismissLoadError() {
        updateContent { it.copy(overlay = null) }
    }

    private fun dismissShareDialog() {
        updateContent { it.copy(overlay = null) }
    }

    private suspend fun shareImage(bitmap: Bitmap, shareText: String, chooserTitle: String) {
        val previewData = when (val currentOverlay = (_uiState.value as? SessionsListUiState.Content)?.overlay) {
            is SessionsListOverlay.SharePreview -> currentOverlay.data
            is SessionsListOverlay.ShareError -> currentOverlay.data
            else -> return
        }
        updateContent { it.copy(overlay = SessionsListOverlay.Sharing(previewData)) }
        val result = imageSharer.shareImage(bitmap, shareText, chooserTitle)
        updateContent { content ->
            when (result) {
                is ShareResult.Success -> content.copy(overlay = SessionsListOverlay.ShareReady(result.intent))
                else -> {
                    val errorMessage = shareErrorMapper.map(result)
                    if (errorMessage != null) {
                        content.copy(overlay = SessionsListOverlay.ShareError(errorMessage, previewData))
                    } else {
                        content.copy(overlay = SessionsListOverlay.SharePreview(previewData))
                    }
                }
            }
        }
    }

    private fun clearShareError() {
        val currentOverlay = (_uiState.value as? SessionsListUiState.Content)?.overlay
        if (currentOverlay is SessionsListOverlay.ShareError) {
            updateContent { it.copy(overlay = SessionsListOverlay.SharePreview(currentOverlay.data)) }
        }
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
