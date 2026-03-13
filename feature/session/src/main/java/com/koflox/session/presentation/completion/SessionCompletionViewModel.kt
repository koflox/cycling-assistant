package com.koflox.session.presentation.completion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.di.SessionErrorMapper
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.bearing.calculateBearingDegrees
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.route.MapLayer
import com.koflox.session.presentation.route.RouteDisplayData
import com.koflox.session.presentation.route.buildRouteDisplayData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SessionCompletionViewModel @Inject constructor(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase,
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase,
    private val sessionUiMapper: SessionUiMapper,
    @param:SessionErrorMapper private val errorMessageMapper: ErrorMessageMapper,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
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
    private var cachedTrackPoints: List<TrackPoint> = emptyList()
    private val routeCache = mutableMapOf<MapLayer, RouteDisplayData>()

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
            observeStatsDisplayConfigUseCase.observeCompletedSessionStats().collect { completedConfig ->
                val session = cachedSession ?: return@collect
                val derivedStats = cachedDerivedStats ?: return@collect
                val completedStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, completedConfig)
                updateContent { it.copy(completedStats = completedStats) }
            }
        }
    }

    fun onEvent(event: SessionCompletionUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is SessionCompletionUiEvent.LayerSelected -> selectLayer(event.layer)
            }
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
                cachedTrackPoints = session.trackPoints
                val routeDisplayData = getOrBuildRouteData(MapLayer.DEFAULT)
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
                val completedStats = sessionUiMapper.buildCompletedSessionStats(session, derivedStats, completedConfig)
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
                    availableLayers = buildAvailableLayers(session.hasPowerData),
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

    private fun buildAvailableLayers(hasPowerData: Boolean): List<MapLayer> = buildList {
        add(MapLayer.DEFAULT)
        add(MapLayer.SPEED)
        if (hasPowerData) add(MapLayer.POWER)
    }

    private fun selectLayer(layer: MapLayer) {
        updateContent { content ->
            content.copy(
                selectedLayer = layer,
                routeDisplayData = getOrBuildRouteData(layer),
            )
        }
    }

    private fun getOrBuildRouteData(layer: MapLayer): RouteDisplayData =
        routeCache.getOrPut(layer) {
            buildRouteDisplayData(cachedTrackPoints, layer.toColorStrategy())
        }
}
