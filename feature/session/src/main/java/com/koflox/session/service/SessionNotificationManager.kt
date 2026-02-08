package com.koflox.session.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.koflox.session.R
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.presentation.mapper.SessionUiMapper

internal interface SessionNotificationManager {
    fun createNotificationChannel()
    fun createInitialNotification(): Notification
    fun buildNotification(session: Session, currentElapsedMs: Long): Notification
}

internal class SessionNotificationManagerImpl(
    private val context: Context,
    private val sessionUiMapper: SessionUiMapper,
) : SessionNotificationManager {

    companion object {
        const val CHANNEL_ID = "cycling_session_channel"
        const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE_CONTENT = 100
        private const val DEFAULT_NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_DEFAULT
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun createInitialNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_cycling)
            .setContentTitle(context.getString(R.string.notification_title_session_active))
            .setContentText(context.getString(R.string.notification_text_starting))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(DEFAULT_NOTIFICATION_PRIORITY)
            .setContentIntent(createContentPendingIntent())
            .build()
    }

    override fun buildNotification(session: Session, currentElapsedMs: Long): Notification {
        val statusText = when (session.status) {
            SessionStatus.RUNNING -> context.getString(R.string.notification_status_running)
            SessionStatus.PAUSED -> context.getString(R.string.notification_status_paused)
            SessionStatus.COMPLETED -> context.getString(R.string.notification_status_paused)
        }
        val elapsedTimeFormatted = sessionUiMapper.formatElapsedTime(currentElapsedMs)
        val distanceFormatted = sessionUiMapper.formatDistance(session.traveledDistanceKm)

        val contentText = "$statusText | $elapsedTimeFormatted"
        val expandedText = buildString {
            appendLine("${context.getString(R.string.notification_time)}: $elapsedTimeFormatted")
            append("${context.getString(R.string.notification_distance)}: $distanceFormatted km")
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_cycling)
            .setContentTitle(session.destinationName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(DEFAULT_NOTIFICATION_PRIORITY)
            .setContentIntent(createContentPendingIntent())
            .addAction(createPauseResumeAction(session.status))
            .addAction(createStopAction())
            .build()
    }

    private fun createContentPendingIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createPauseResumeAction(status: SessionStatus): NotificationCompat.Action {
        return if (status == SessionStatus.RUNNING) {
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification_pause,
                context.getString(R.string.notification_action_pause),
                createActionPendingIntent(SessionTrackingService.ACTION_PAUSE),
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.notification_action_resume),
                createActionPendingIntent(SessionTrackingService.ACTION_RESUME),
            ).build()
        }
    }

    private fun createStopAction(): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification_stop,
            context.getString(R.string.notification_action_stop),
            createActionPendingIntent(SessionTrackingService.ACTION_STOP),
        ).build()
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, SessionTrackingService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

}
