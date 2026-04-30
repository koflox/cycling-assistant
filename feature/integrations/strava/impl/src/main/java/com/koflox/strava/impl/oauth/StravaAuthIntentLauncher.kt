package com.koflox.strava.impl.oauth

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import javax.inject.Inject

internal class StravaAuthIntentLauncher @Inject constructor(
    private val authUrlBuilder: StravaAuthUrlBuilder,
) {

    /**
     * Launches the Strava OAuth flow in Custom Tabs with [Intent.FLAG_ACTIVITY_NO_HISTORY] so
     * the browser is not retained in the task back stack. Without this flag, after Strava
     * redirects to our `cyclingassistant://...` deep link, [StravaOAuthRedirectActivity]
     * finishes and the user is left looking at the now-empty Custom Tabs activity (still on
     * top of the task) until they press back. With the flag, Custom Tabs is removed from
     * history when the user navigates away — including when our deep-link activity takes
     * over — so the user sees `MainActivity` immediately with the updated auth state.
     */
    fun launch(context: Context) {
        val tabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .build()
        tabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        tabsIntent.launchUrl(context, authUrlBuilder.build().toUri())
    }
}
