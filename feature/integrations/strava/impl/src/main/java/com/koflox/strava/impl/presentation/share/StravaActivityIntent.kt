package com.koflox.strava.impl.presentation.share

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

internal object StravaActivityIntent {

    private const val APP_PACKAGE = "com.strava"
    private const val WEB_BASE = "https://www.strava.com/activities/"
    private const val APP_DEEP_LINK = "strava://activities/"

    fun open(context: Context, activityId: Long) {
        val intent = appIntent(context, activityId) ?: webIntent(activityId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun appIntent(context: Context, activityId: Long): Intent? {
        val uri = "$APP_DEEP_LINK$activityId".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).setPackage(APP_PACKAGE)
        return if (intent.resolveActivity(context.packageManager) != null) intent else null
    }

    private fun webIntent(activityId: Long): Intent =
        Intent(Intent.ACTION_VIEW, "$WEB_BASE$activityId".toUri())
}
