package com.koflox.session.presentation.share

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.location.bearing.calculateBearingDegrees
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.route.buildRouteDisplayData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShareViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase,
    private val sessionUiMapper: SessionUiMapper,
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase,
    private val imageSharer: SessionImageSharer,
    private val shareErrorMapper: ShareErrorMapper,
    private val gpxMapper: GpxMapper,
    private val gpxSharer: SessionGpxSharer,
    private val gpxShareErrorMapper: GpxShareErrorMapper,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    companion object {
        private const val JOULES_PER_KCAL = 1000.0
        private const val GPX_FILE_NAME = "session_export"
    }

    private val sessionId: String = checkNotNull(savedStateHandle[SESSION_ID_ARG])

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Loading)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private var cachedSession: Session? = null
    private var cachedDerivedStats: SessionDerivedStats? = null
    private var gpxGenerationJob: Job? = null

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            loadSession()
        }
    }

    fun onEvent(event: ShareUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is ShareUiEvent.TabSelected -> updateContent { it.copy(selectedTab = event.tab) }
                is ShareUiEvent.Image.ShareConfirmed -> shareImage(event.bitmap, event.shareText, event.chooserTitle)
                ShareUiEvent.Image.IntentLaunched -> updateContent { it.copy(imageShareState = ImageShareState.Idle) }
                ShareUiEvent.Image.ErrorDismissed -> updateContent { it.copy(imageShareState = ImageShareState.Idle) }
                ShareUiEvent.Gpx.ShareClicked -> shareGpx()
                ShareUiEvent.Gpx.IntentLaunched -> updateContent { it.copy(gpxShareState = GpxShareState.Idle) }
                ShareUiEvent.Gpx.ErrorDismissed -> updateContent { it.copy(gpxShareState = GpxShareState.Idle) }
            }
        }
    }

    private suspend fun loadSession() {
        getSessionByIdUseCase.getSession(sessionId)
            .onSuccess { session ->
                val formattedData = sessionUiMapper.toSessionUiModel(session)
                val derivedStats = calculateSessionStatsUseCase.calculate(sessionId).getOrNull() ?: return@onSuccess
                cachedSession = session
                cachedDerivedStats = derivedStats
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
                val gpxAvailable = gpxSharer.isGpxSharingAvailable()
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
                _uiState.value = ShareUiState.Content(
                    sharePreviewData = previewData,
                    gpxShareState = if (gpxAvailable) GpxShareState.Idle else GpxShareState.Unavailable,
                )
                observeShareStatsConfig()
            }
    }

    private fun observeShareStatsConfig() {
        viewModelScope.launch(dispatcherDefault) {
            observeStatsDisplayConfigUseCase.observeShareStats().collect { shareConfig ->
                val session = cachedSession ?: return@collect
                val derivedStats = cachedDerivedStats ?: return@collect
                val shareStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, shareConfig)
                updateContent { it.copy(sharePreviewData = it.sharePreviewData.copy(shareStats = shareStats)) }
            }
        }
    }

    private suspend fun shareImage(bitmap: Bitmap, shareText: String, chooserTitle: String) {
        updateContent { it.copy(imageShareState = ImageShareState.Sharing) }
        val result = imageSharer.shareImage(bitmap, shareText, chooserTitle)
        updateContent { content ->
            when (result) {
                is ShareResult.Success -> content.copy(imageShareState = ImageShareState.Ready(result.intent))
                else -> {
                    val errorMessage = shareErrorMapper.map(result)
                    if (errorMessage != null) {
                        content.copy(imageShareState = ImageShareState.Error(errorMessage))
                    } else {
                        content.copy(imageShareState = ImageShareState.Idle)
                    }
                }
            }
        }
    }

    private fun shareGpx() {
        val session = cachedSession ?: return
        gpxGenerationJob?.cancel()
        gpxGenerationJob = viewModelScope.launch(dispatcherDefault) {
            updateContent { it.copy(gpxShareState = GpxShareState.Generating) }
            val gpxContent = gpxMapper.map(session)
            val chooserTitle = session.destinationName ?: "Session GPX"
            val result = gpxSharer.shareGpx(gpxContent, GPX_FILE_NAME, chooserTitle)
            updateContent { content ->
                when (result) {
                    is GpxShareResult.Success -> content.copy(gpxShareState = GpxShareState.Ready(result.intent))
                    else -> {
                        val errorMessage = gpxShareErrorMapper.map(result)
                        if (errorMessage != null) {
                            content.copy(gpxShareState = GpxShareState.Error(errorMessage))
                        } else {
                            content.copy(gpxShareState = GpxShareState.Idle)
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gpxGenerationJob?.cancel()
    }

    private inline fun updateContent(transform: (ShareUiState.Content) -> ShareUiState.Content) {
        val current = _uiState.value
        if (current is ShareUiState.Content) {
            _uiState.value = transform(current)
        }
    }
}
