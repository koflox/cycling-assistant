package com.koflox.session.service

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.location.usecase.CheckLocationEnabledUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.domain.usecase.comparison.ComparisonSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal interface LocationCollectionManager {
    fun start(scope: CoroutineScope)
    fun stop()
}

internal class LocationCollectionManagerImpl(
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val updateSessionLocationUseCase: UpdateSessionLocationUseCase,
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val currentTimeProvider: CurrentTimeProvider,
    private val comparisonSessionManager: ComparisonSessionManager,
    private val activeSessionUseCase: ActiveSessionUseCase,
) : LocationCollectionManager {

    companion object {
        internal val LOCATION_INTERVAL = 5.seconds
        internal val LOCATION_MAX_UPDATE_DELAY = 15.seconds
        internal const val MIN_UPDATE_DISTANCE_METERS = 5F
    }

    private var locationCollectionJob: Job? = null
    private var locationMonitorJob: Job? = null

    override fun start(scope: CoroutineScope) {
        startLocationCollection(scope)
        startLocationMonitoring(scope)
    }

    override fun stop() {
        locationCollectionJob?.cancel()
        locationCollectionJob = null
        locationMonitorJob?.cancel()
        locationMonitorJob = null
    }

    private fun startLocationCollection(scope: CoroutineScope) {
        if (locationCollectionJob?.isActive == true) return
        locationCollectionJob = scope.launch {
            observeUserLocationUseCase.observe(
                intervalMs = LOCATION_INTERVAL.inWholeMilliseconds,
                minUpdateDistanceMeters = MIN_UPDATE_DISTANCE_METERS,
                maxUpdateDelayMs = LOCATION_MAX_UPDATE_DELAY.inWholeMilliseconds,
            ).collect { location ->
                val timestampMs = currentTimeProvider.currentTimeMs()
                updateSessionLocationUseCase.update(
                    location = location,
                    timestampMs = timestampMs,
                )
                try {
                    val session = activeSessionUseCase.getActiveSession()
                    comparisonSessionManager.onLocationUpdate(location, timestampMs, session)
                } catch (_: Exception) {
                    // No active session — skip comparison
                }
            }
        }
    }

    private fun startLocationMonitoring(scope: CoroutineScope) {
        if (locationMonitorJob?.isActive == true) return
        locationMonitorJob = scope.launch {
            checkLocationEnabledUseCase.observeLocationEnabled().collect { isEnabled ->
                if (!isEnabled) {
                    updateSessionStatusUseCase.pause()
                }
            }
        }
    }
}
