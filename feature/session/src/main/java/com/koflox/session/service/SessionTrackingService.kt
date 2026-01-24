package com.koflox.session.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.koflox.concurrent.DispatchersQualifier
import com.koflox.location.LocationDataSource
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
import org.koin.android.ext.android.inject

class SessionTrackingService : Service() {

    companion object {
        const val ACTION_START = "com.koflox.session.START"
        const val ACTION_STOP = "com.koflox.session.STOP"
        const val ACTION_PAUSE = "com.koflox.session.PAUSE"
        const val ACTION_RESUME = "com.koflox.session.RESUME"

        private const val LOCATION_INTERVAL_MS = 3000L
        private const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }

    private val activeSessionUseCase: ActiveSessionUseCase by inject()
    private val updateSessionLocationUseCase: UpdateSessionLocationUseCase by inject()
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase by inject()
    private val locationDataSource: LocationDataSource by inject()
    private val notificationManager: SessionNotificationManager by inject()
    private val dispatcherIo: CoroutineDispatcher by inject(DispatchersQualifier.Io)

    private val serviceScope by lazy { CoroutineScope(SupervisorJob() + dispatcherIo) }
    private var sessionObserverJob: Job? = null
    private var locationCollectionJob: Job? = null
    private var timerJob: Job? = null

    private val androidNotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> handleStop()
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            null -> handleRestart()
        }
        return START_STICKY
    }

    private fun startTracking() {
        val notification = notificationManager.createInitialNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                SessionNotificationManagerImpl.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(SessionNotificationManagerImpl.NOTIFICATION_ID, notification)
        }
        observeSession()
    }

    private fun handleRestart() {
        serviceScope.launch {
            val activeSession = activeSessionUseCase.observeActiveSession().first()
            if (activeSession != null) {
                startTracking()
            } else {
                stopSelf()
            }
        }
    }

    private fun observeSession() {
        sessionObserverJob?.cancel()
        sessionObserverJob = serviceScope.launch {
            activeSessionUseCase.observeActiveSession().collect { session ->
                if (session != null) {
                    handleSessionUpdate(session)
                } else {
                    stopSelf()
                }
            }
        }
    }

    private fun handleSessionUpdate(session: Session) {
        when (session.status) {
            SessionStatus.RUNNING -> {
                startLocationCollection()
                startTimer(session)
            }

            SessionStatus.PAUSED -> {
                stopLocationCollection()
                stopTimer()
                updateNotificationWithSession(session, session.elapsedTimeMs)
            }

            SessionStatus.COMPLETED -> {
                stopLocationCollection()
                stopTimer()
                stopSelf()
            }
        }
    }

    private fun startLocationCollection() {
        if (locationCollectionJob?.isActive == true) return
        locationCollectionJob = serviceScope.launch {
            while (isActive) {
                delay(LOCATION_INTERVAL_MS)
                locationDataSource.getCurrentLocation()
                    .onSuccess { location ->
                        updateSessionLocationUseCase.update(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestampMs = System.currentTimeMillis(),
                            altitudeMeters = location.altitudeMeters,
                        )
                    }
            }
        }
    }

    private fun stopLocationCollection() {
        locationCollectionJob?.cancel()
        locationCollectionJob = null
    }

    private fun startTimer(session: Session) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val elapsedSinceLastResume = System.currentTimeMillis() - session.lastResumedTimeMs
                val totalElapsedMs = session.elapsedTimeMs + elapsedSinceLastResume
                updateNotificationWithSession(session, totalElapsedMs)
                delay(TIMER_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateNotificationWithSession(session: Session, elapsedMs: Long) {
        val notification = notificationManager.buildNotification(session, elapsedMs)
        androidNotificationManager.notify(SessionNotificationManagerImpl.NOTIFICATION_ID, notification)
    }

    private fun handlePause() {
        serviceScope.launch {
            updateSessionStatusUseCase.pause()
        }
    }

    private fun handleResume() {
        serviceScope.launch {
            updateSessionStatusUseCase.resume()
        }
    }

    private fun handleStop() {
        serviceScope.launch {
            updateSessionStatusUseCase.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionObserverJob?.cancel()
        locationCollectionJob?.cancel()
        timerJob?.cancel()
    }

}
