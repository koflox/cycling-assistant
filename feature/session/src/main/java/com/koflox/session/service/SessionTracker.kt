package com.koflox.session.service

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.location.usecase.CheckLocationEnabledUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCase
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

interface SessionTrackingDelegate {
    fun onStartForeground(): Boolean
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
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
    private val nutritionReminderUseCase: NutritionReminderUseCase,
    private val powerCollectionManager: PowerCollectionManager,
    private val currentTimeProvider: CurrentTimeProvider,
) : SessionTracker {

    companion object {
        internal val LOCATION_INTERVAL = 5.seconds
        internal val LOCATION_MAX_UPDATE_DELAY = 15.seconds
        internal const val MIN_UPDATE_DISTANCE_METERS = 5F
        internal val TIMER_UPDATE_INTERVAL = 1.seconds
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
                if (activeSession != null && delegate.onStartForeground()) {
                    updateSessionStatusUseCase.onServiceRestart()
                    observeSession()
                    observeNutritionReminders()
                } else {
                    delegate.onStopService()
                }
            }
        }
    }

    override fun stopTracking() {
        powerCollectionManager.stop()
        scope?.cancel()
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
                    stopLocationCollection()
                    stopLocationMonitoring()
                    stopTimer()
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
                scope?.let { powerCollectionManager.start(it) }
            }

            SessionStatus.PAUSED -> {
                stopLocationCollection()
                stopLocationMonitoring()
                stopTimer()
                powerCollectionManager.stop()
                delegate?.onNotificationUpdate(session, session.elapsedTimeMs)
            }

            SessionStatus.COMPLETED -> {
                stopLocationCollection()
                stopLocationMonitoring()
                stopTimer()
                powerCollectionManager.stop()
                delegate?.onStopService()
            }
        }
    }

    private fun startLocationCollection() {
        if (locationCollectionJob?.isActive == true) return
        locationCollectionJob = scope?.launch {
            observeUserLocationUseCase.observe(
                intervalMs = LOCATION_INTERVAL.inWholeMilliseconds,
                minUpdateDistanceMeters = MIN_UPDATE_DISTANCE_METERS,
                maxUpdateDelayMs = LOCATION_MAX_UPDATE_DELAY.inWholeMilliseconds,
            ).collect { location ->
                updateSessionLocationUseCase.update(
                    location = location,
                    timestampMs = currentTimeProvider.currentTimeMs(),
                )
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
            checkLocationEnabledUseCase.observeLocationEnabled().collect { isEnabled ->
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
                val elapsedSinceLastResume = currentTimeProvider.currentTimeMs() - session.lastResumedTimeMs
                val totalElapsedMs = session.elapsedTimeMs + elapsedSinceLastResume
                delegate?.onNotificationUpdate(session, totalElapsedMs)
                delay(TIMER_UPDATE_INTERVAL)
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
