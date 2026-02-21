package com.koflox.session.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import com.koflox.session.domain.model.Session
import org.koin.android.ext.android.inject

class SessionTrackingService : Service(), SessionTrackingDelegate {

    companion object {
        const val ACTION_START = "com.koflox.session.START"
        const val ACTION_PAUSE = "com.koflox.session.PAUSE"
        const val ACTION_RESUME = "com.koflox.session.RESUME"

        private const val VIBRATION_DURATION_MS = 200L
    }

    private val sessionTracker: SessionTracker by inject()
    private val notificationManager: SessionNotificationManager by inject()

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
            ACTION_START -> {
                if (goForeground()) {
                    sessionTracker.startTracking(this)
                }
            }
            ACTION_PAUSE -> sessionTracker.pauseSession()
            ACTION_RESUME -> sessionTracker.resumeSession()
            null -> sessionTracker.handleRestart(this)
        }
        return START_STICKY
    }

    override fun onStartForeground(): Boolean = goForeground()

    override fun onNotificationUpdate(session: Session, elapsedMs: Long) {
        val notification = notificationManager.buildNotification(session, elapsedMs)
        androidNotificationManager.notify(SessionNotificationManagerImpl.NOTIFICATION_ID, notification)
    }

    override fun onStopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onVibrate() {
        vibrate()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionTracker.stopTracking()
    }

    private fun goForeground(): Boolean {
        val notification = notificationManager.createInitialNotification()
        return try {
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
            true
        } catch (_: SecurityException) {
            stopSelf()
            false
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATION_DURATION_MS)
        }
    }
}
