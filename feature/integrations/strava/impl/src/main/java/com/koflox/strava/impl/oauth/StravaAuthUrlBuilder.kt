package com.koflox.strava.impl.oauth

import com.koflox.strava.impl.data.api.StravaClientCredentials
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import javax.inject.Inject

internal class StravaAuthUrlBuilder @Inject constructor(
    private val clientCredentials: StravaClientCredentials,
) {

    companion object {
        const val REDIRECT_HOST = "koflox.github.io"
        const val REDIRECT_PATH = "/strava/callback"
        const val REDIRECT_SCHEME = "cyclingassistant"
        const val REDIRECT_URI = "$REDIRECT_SCHEME://$REDIRECT_HOST$REDIRECT_PATH"

        /**
         * Scopes the app cannot operate without — `activity:read` and `activity:write` are
         * required for the core Strava sync flow. `read` covers basic athlete data. The
         * Strava consent UI presents these as user-toggleable checkboxes (there is no API
         * to make them mandatory), so the redirect handler must re-validate the granted
         * scopes returned in the callback URL.
         */
        val REQUIRED_SCOPES: Set<String> = setOf("activity:read", "activity:write")

        private const val AUTH_HOST = "www.strava.com"
        private val AUTH_PATH_SEGMENTS = listOf("oauth", "authorize")
        private val SCOPES = listOf("read") + REQUIRED_SCOPES
    }

    fun build(): String = URLBuilder(
        protocol = URLProtocol.HTTPS,
        host = AUTH_HOST,
        pathSegments = AUTH_PATH_SEGMENTS,
    ).apply {
        parameters.append("client_id", clientCredentials.clientId)
        parameters.append("response_type", "code")
        parameters.append("redirect_uri", REDIRECT_URI)
        parameters.append("approval_prompt", "force")
        parameters.append("scope", SCOPES.joinToString(separator = ","))
    }.buildString()

}
