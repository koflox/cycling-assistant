package com.koflox.session.service

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
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

/**
 * Callback interface for [SessionTrackingService] to respond to tracking events
 * such as foreground promotion, notification updates, service teardown, and vibration.
 */
internal interface SessionTrackingDelegate {
    fun onStartForeground(): Boolean
    fun onNotificationUpdate(session: Session, elapsedMs: Long)
    fun onStopService()
    fun onVibrate()
}

/**
 * Orchestrates an active cycling session by observing session state changes
 * and delegating work to specialized managers:
 * - [LocationCollectionManager] — GPS tracking and location-enabled monitoring
 * - [PowerCollectionManager] — BLE power meter data collection with reconnection
 * - [NutritionReminderManager] — periodic nutrition reminder events
 *
 * The tracker itself owns the notification timer and session lifecycle commands
 * (pause / resume / stop).
 */
internal interface SessionTracker {
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
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val locationCollectionManager: LocationCollectionManager,
    private val powerCollectionManager: PowerCollectionManager,
    private val nutritionReminderManager: NutritionReminderManager,
    private val currentTimeProvider: CurrentTimeProvider,
) : SessionTracker {

    companion object {
        internal val TIMER_UPDATE_INTERVAL = 1.seconds
    }

    private var scope: CoroutineScope? = null
    private var delegate: SessionTrackingDelegate? = null
    private var sessionObserverJob: Job? = null
    private var timerJob: Job? = null

    override fun startTracking(delegate: SessionTrackingDelegate) {
        this.delegate = delegate
        scope = CoroutineScope(SupervisorJob() + dispatcherIo)
        observeSession()
        scope?.let { nutritionReminderManager.start(it) { this.delegate?.onVibrate() } }
    }

    override fun handleRestart(delegate: SessionTrackingDelegate) {
        this.delegate = delegate
        scope = CoroutineScope(SupervisorJob() + dispatcherIo).also {
            it.launch {
                val activeSession = activeSessionUseCase.observeActiveSession().first()
                if (activeSession != null && delegate.onStartForeground()) {
                    updateSessionStatusUseCase.onServiceRestart()
                    observeSession()
                    nutritionReminderManager.start(it) { this@SessionTrackerImpl.delegate?.onVibrate() }
                } else {
                    delegate.onStopService()
                }
            }
        }
    }

    override fun stopTracking() {
        locationCollectionManager.stop()
        powerCollectionManager.stop()
        nutritionReminderManager.stop()
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
                    locationCollectionManager.stop()
                    powerCollectionManager.stop()
                    stopTimer()
                    delegate?.onStopService()
                }
            }
        }
    }

    private fun handleSessionUpdate(session: Session) {
        when (session.status) {
            SessionStatus.RUNNING -> {
                scope?.let(locationCollectionManager::start)
                scope?.let(powerCollectionManager::start)
                startTimer(session)
            }

            SessionStatus.PAUSED -> {
                locationCollectionManager.stop()
                powerCollectionManager.stop()
                stopTimer()
                delegate?.onNotificationUpdate(session, session.elapsedTimeMs)
            }

            SessionStatus.COMPLETED -> Unit
        }
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
}
