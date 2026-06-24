package com.koflox.strava.impl.presentation.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.usecase.ObserveStravaSyncStatusUseCase
import com.koflox.strava.api.usecase.StravaSyncUseCase
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class StravaShareViewModel @Inject constructor(
    private val authUseCase: StravaAuthUseCase,
    private val syncUseCase: StravaSyncUseCase,
    private val observeSyncStatusUseCase: ObserveStravaSyncStatusUseCase,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private companion object {
        val REFRESH_COOLDOWN = 10.seconds
        val TICK_INTERVAL = 1.seconds
    }

    private val sessionIdFlow = MutableStateFlow<String?>(null)
    private val refreshCooldownFlow = MutableStateFlow(0)

    private val _uiState = MutableStateFlow<StravaShareUiState>(StravaShareUiState.Loading)
    val uiState: StateFlow<StravaShareUiState> = _uiState.asStateFlow()

    private val navigationChannel = Channel<StravaShareNavigation>(Channel.BUFFERED)
    val navigation: Flow<StravaShareNavigation> = navigationChannel.receiveAsFlow()

    private var cooldownJob: Job? = null

    init {
        observeStateForSession()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeStateForSession() {
        viewModelScope.launch(dispatcherDefault) {
            sessionIdFlow.filterNotNull().flatMapLatest { sessionId ->
                combine(
                    authUseCase.observeAuthState(),
                    observeSyncStatusUseCase.observe(sessionId),
                    refreshCooldownFlow,
                ) { authState, syncStatus, cooldown ->
                    StravaShareUiState.Content(
                        authState = authState,
                        syncStatus = syncStatus,
                        refreshCooldownSeconds = cooldown,
                    )
                }
            }.collect { _uiState.value = it }
        }
    }

    fun onEvent(event: StravaShareUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is StravaShareUiEvent.Started -> {
                    sessionIdFlow.value = event.sessionId
                    syncUseCase.verifySyncedActivity(event.sessionId)
                }
                StravaShareUiEvent.ConnectClicked -> navigationChannel.send(StravaShareNavigation.LaunchOAuthIntent)
                StravaShareUiEvent.SyncClicked -> sessionIdFlow.value?.let { syncUseCase.enqueue(it) }
                StravaShareUiEvent.RetryClicked -> sessionIdFlow.value?.let { syncUseCase.retry(it) }
                StravaShareUiEvent.RefreshClicked -> handleRefresh()
                StravaShareUiEvent.ViewOnStravaClicked -> handleViewOnStrava()
            }
        }
    }

    private suspend fun handleRefresh() {
        val sessionId = sessionIdFlow.value ?: return
        if (refreshCooldownFlow.value > 0) return
        startRefreshCooldown()
        syncUseCase.refreshStatus(sessionId)
    }

    private fun startRefreshCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch(dispatcherDefault) {
            var remaining = REFRESH_COOLDOWN.inWholeSeconds.toInt()
            while (remaining > 0) {
                refreshCooldownFlow.value = remaining
                delay(TICK_INTERVAL.inWholeMilliseconds)
                remaining--
            }
            refreshCooldownFlow.value = 0
        }
    }

    private suspend fun handleViewOnStrava() {
        val activityId = (uiState.value as? StravaShareUiState.Content)
            ?.syncStatus?.let { it as? SessionSyncStatus.Synced }?.activityId
            ?: return
        navigationChannel.send(StravaShareNavigation.OpenStravaActivity(activityId))
    }
}
