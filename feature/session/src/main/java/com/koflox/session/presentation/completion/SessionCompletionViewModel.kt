package com.koflox.session.presentation.completion

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.bearing.calculateBearingDegrees
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.route.buildRouteDisplayData
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.SharePreviewData
import com.koflox.session.presentation.share.ShareResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class SessionCompletionViewModel(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase,
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase,
    private val sessionUiMapper: SessionUiMapper,
    private val errorMessageMapper: ErrorMessageMapper,
    private val imageSharer: SessionImageSharer,
    private val shareErrorMapper: ShareErrorMapper,
    private val dispatcherDefault: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val JOULES_PER_KCAL = 1000.0
    }

    private val sessionId: String = checkNotNull(savedStateHandle[SESSION_ID_ARG])

    private val _uiState = MutableStateFlow<SessionCompletionUiState>(SessionCompletionUiState.Loading)
    val uiState: StateFlow<SessionCompletionUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<SessionCompletionNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private var cachedSession: Session? = null
    private var cachedDerivedStats: SessionDerivedStats? = null

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            loadSession()
        }
        observeStatsConfig()
    }

    private fun observeStatsConfig() {
        viewModelScope.launch(dispatcherDefault) {
            combine(
                observeStatsDisplayConfigUseCase.observeCompletedSessionStats(),
                observeStatsDisplayConfigUseCase.observeShareStats(),
            ) { completedConfig, shareConfig ->
                completedConfig to shareConfig
            }.collect { (completedConfig, shareConfig) ->
                val session = cachedSession ?: return@collect
                val derivedStats = cachedDerivedStats ?: return@collect
                val completedStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, completedConfig)
                val shareStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, shareConfig)
                updateContent { it.copy(completedStats = completedStats, shareStats = shareStats) }
            }
        }
    }

    fun onEvent(event: SessionCompletionUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                SessionCompletionUiEvent.ShareClicked -> showShareDialog()
                is SessionCompletionUiEvent.ShareConfirmed -> shareImage(event.bitmap, event.shareText, event.chooserTitle)
                SessionCompletionUiEvent.ShareDialogDismissed -> dismissShareDialog()
                SessionCompletionUiEvent.ShareIntentLaunched -> clearShareIntent()
                SessionCompletionUiEvent.ErrorDismissed -> clearOverlayError()
            }
        }
    }

    private fun showShareDialog() {
        updateContent { content ->
            content.copy(overlay = Overlay.ShareDialog(buildSharePreviewData(content)))
        }
    }

    private fun dismissShareDialog() {
        updateContent { it.copy(overlay = null) }
    }

    private suspend fun shareImage(bitmap: Bitmap, shareText: String, chooserTitle: String) {
        updateContent { it.copy(overlay = Overlay.Sharing(buildSharePreviewData(it))) }
        val result = imageSharer.shareImage(bitmap, shareText, chooserTitle)
        updateContent { content ->
            when (result) {
                is ShareResult.Success -> content.copy(overlay = Overlay.ShareReady(result.intent))
                else -> {
                    val errorMessage = shareErrorMapper.map(result)
                    content.copy(
                        overlay = if (errorMessage != null) {
                            Overlay.ShareError(errorMessage)
                        } else {
                            Overlay.ShareDialog(buildSharePreviewData(content))
                        },
                    )
                }
            }
        }
    }

    private fun clearShareIntent() {
        updateContent { it.copy(overlay = null) }
    }

    private fun clearOverlayError() {
        updateContent { content ->
            val newOverlay = if (content.overlay is Overlay.ShareError) {
                Overlay.ShareDialog(buildSharePreviewData(content))
            } else {
                content.overlay
            }
            content.copy(overlay = newOverlay)
        }
    }

    @Suppress("CyclomaticComplexity")
    private suspend fun loadSession() {
        getSessionByIdUseCase.getSession(sessionId)
            .onSuccess { session ->
                if (session.status != SessionStatus.COMPLETED) {
                    _navigation.send(SessionCompletionNavigation.ToDashboard)
                    return@onSuccess
                }
                val formattedData = sessionUiMapper.toSessionUiModel(session)
                val derivedStats = calculateSessionStatsUseCase.calculate(sessionId).getOrNull() ?: return@onSuccess
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
                cachedSession = session
                cachedDerivedStats = derivedStats
                val completedConfig = observeStatsDisplayConfigUseCase.observeCompletedSessionStats().first()
                val shareConfig = observeStatsDisplayConfigUseCase.observeShareStats().first()
                val completedStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, completedConfig)
                val shareStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, shareConfig)
                _uiState.value = SessionCompletionUiState.Content(
                    sessionId = sessionId,
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
                    completedStats = completedStats,
                    shareStats = shareStats,
                    routeDisplayData = routeDisplayData,
                    endMarkerRotation = endRotation,
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

    private fun buildSharePreviewData(content: SessionCompletionUiState.Content) = SharePreviewData(
        sessionId = content.sessionId,
        destinationName = content.destinationName,
        startDateFormatted = content.startDateFormatted,
        elapsedTimeFormatted = content.elapsedTimeFormatted,
        movingTimeFormatted = content.movingTimeFormatted,
        idleTimeFormatted = content.idleTimeFormatted,
        traveledDistanceFormatted = content.traveledDistanceFormatted,
        averageSpeedFormatted = content.averageSpeedFormatted,
        topSpeedFormatted = content.topSpeedFormatted,
        altitudeGainFormatted = content.altitudeGainFormatted,
        altitudeLossFormatted = content.altitudeLossFormatted,
        caloriesFormatted = content.caloriesFormatted,
        averagePowerFormatted = content.averagePowerFormatted,
        maxPowerFormatted = content.maxPowerFormatted,
        shareStats = content.shareStats,
        routeDisplayData = content.routeDisplayData,
        endMarkerRotation = content.endMarkerRotation,
    )
}
