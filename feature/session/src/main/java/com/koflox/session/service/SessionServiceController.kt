package com.koflox.session.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

interface SessionServiceController {
    fun startSessionTracking()
}

internal class SessionServiceControllerImpl(
    private val context: Context,
) : SessionServiceController {

    override fun startSessionTracking() {
        val intent = Intent(context, SessionTrackingService::class.java).apply {
            action = SessionTrackingService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }
}
