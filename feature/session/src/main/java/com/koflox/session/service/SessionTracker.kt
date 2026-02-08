package com.koflox.session.service

import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.settings.LocationSettingsDataSource
import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface SessionTrackingDelegate {
    fun onStartForeground()
    fun onNotificationUpdate(session: Session, elapsedMs: Long)
    fun onStopService()
    fun onVibrate()
}

interface SessionTracker {
    fun startTracking(delegate: SessionTrackingDelegate)
    fun handleRestart(delegate: SessionTrackingDelegate)
    fun stopTracking()
    fun pauseSession()
    fun resumeSession()
    fun stopSession()
}

internal class SessionTrackerImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val updateSessionLocationUseCase: UpdateSessionLocationUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val locationDataSource: LocationDataSource,
    private val locationSettingsDataSource: LocationSettingsDataSource,
    private val nutritionReminderUseCase: NutritionReminderUseCase,
    private val currentTimeProvider: () -> Long = { System.currentTimeMillis() },
) : SessionTracker {

    companion object {
        internal const val LOCATION_INTERVAL_MS = 3000L
        internal const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }

    private var scope: CoroutineScope? = null
    private var delegate: SessionTrackingDelegate? = null
    private var sessionObserverJob: Job? = null
    private var locationCollectionJob: Job? = null
    private var locationMonitorJob: Job? = null
    private var timerJob: Job? = null
    private var nutritionJob: Job? = null

    override fun startTracking(delegate: SessionTrackingDelegate) {
        this.delegate = delegate
        scope = CoroutineScope(SupervisorJob() + dispatcherIo)
        observeSession()
        observeNutritionReminders()
    }

    override fun handleRestart(delegate: SessionTrackingDelegate) {
        this.delegate = delegate
        scope = CoroutineScope(SupervisorJob() + dispatcherIo).also {
            it.launch {
                val activeSession = activeSessionUseCase.observeActiveSession().first()
                if (activeSession != null) {
                    delegate.onStartForeground()
                    observeSession()
                    observeNutritionReminders()
                } else {
                    delegate.onStopService()
                }
            }
        }
    }

    override fun stopTracking() {
        sessionObserverJob?.cancel()
        locationCollectionJob?.cancel()
        locationMonitorJob?.cancel()
        timerJob?.cancel()
        nutritionJob?.cancel()
        scope?.let {
            val job = it.coroutineContext[Job]
            job?.cancel()
        }
        scope = null
        delegate = null
    }

    override fun pauseSession() {
        scope?.launch { updateSessionStatusUseCase.pause() }
    }

    override fun resumeSession() {
        scope?.launch { updateSessionStatusUseCase.resume() }
    }

    override fun stopSession() {
        scope?.launch { updateSessionStatusUseCase.stop() }
    }

    private fun observeSession() {
        sessionObserverJob?.cancel()
        sessionObserverJob = scope?.launch {
            activeSessionUseCase.observeActiveSession().collect { session ->
                if (session != null) {
                    handleSessionUpdate(session)
                } else {
                    delegate?.onStopService()
                }
            }
        }
    }

    private fun handleSessionUpdate(session: Session) {
        when (session.status) {
            SessionStatus.RUNNING -> {
                startLocationCollection()
                startLocationMonitoring()
                startTimer(session)
            }

            SessionStatus.PAUSED -> {
                stopLocationCollection()
                stopLocationMonitoring()
                stopTimer()
                delegate?.onNotificationUpdate(session, session.elapsedTimeMs)
            }

            SessionStatus.COMPLETED -> {
                stopLocationCollection()
                stopLocationMonitoring()
                stopTimer()
                delegate?.onStopService()
            }
        }
    }

    private fun startLocationCollection() {
        if (locationCollectionJob?.isActive == true) return
        locationCollectionJob = scope?.launch {
            while (isActive) {
                delay(LOCATION_INTERVAL_MS)
                locationDataSource.getCurrentLocation()
                    .onSuccess { location ->
                        updateSessionLocationUseCase.update(
                            location = location,
                            timestampMs = currentTimeProvider(),
                        )
                    }
            }
        }
    }

    private fun stopLocationCollection() {
        locationCollectionJob?.cancel()
        locationCollectionJob = null
    }

    private fun startLocationMonitoring() {
        if (locationMonitorJob?.isActive == true) return
        locationMonitorJob = scope?.launch {
            locationSettingsDataSource.observeLocationEnabled().collect { isEnabled ->
                if (!isEnabled) {
                    updateSessionStatusUseCase.pause()
                }
            }
        }
    }

    private fun stopLocationMonitoring() {
        locationMonitorJob?.cancel()
        locationMonitorJob = null
    }

    private fun startTimer(session: Session) {
        timerJob?.cancel()
        timerJob = scope?.launch {
            while (isActive) {
                val elapsedSinceLastResume = currentTimeProvider() - session.lastResumedTimeMs
                val totalElapsedMs = session.elapsedTimeMs + elapsedSinceLastResume
                delegate?.onNotificationUpdate(session, totalElapsedMs)
                delay(TIMER_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun observeNutritionReminders() {
        nutritionJob?.cancel()
        nutritionJob = scope?.launch {
            nutritionReminderUseCase.observeNutritionReminders().collect {
                delegate?.onVibrate()
            }
        }
    }
}
