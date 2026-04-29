package com.koflox.strava.impl.oauth

import com.koflox.strava.impl.data.api.StravaClientCredentials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StravaAuthUrlBuilderTest {

    companion object {
        private const val CLIENT_ID = "12345"
        private const val CLIENT_SECRET = "secret"
    }

    private val builder = StravaAuthUrlBuilder(
        clientCredentials = StravaClientCredentials(clientId = CLIENT_ID, clientSecret = CLIENT_SECRET),
    )

    @Test
    fun `build produces HTTPS URL pointing at strava authorize endpoint`() {
        val url = builder.build()

        assertTrue(url.startsWith("https://www.strava.com/oauth/authorize?"))
    }

    @Test
    fun `build appends client_id from credentials`() {
        val url = builder.build()

        assertTrue(url.contains("client_id=$CLIENT_ID"))
    }

    @Test
    fun `build uses authorization_code response type`() {
        val url = builder.build()

        assertTrue(url.contains("response_type=code"))
    }

    @Test
    fun `build embeds redirect URI with custom scheme and koflox host`() {
        val url = builder.build()

        assertTrue(url.contains("redirect_uri=cyclingassistant%3A%2F%2Fkoflox.github.io%2Fstrava%2Fcallback"))
    }

    @Test
    fun `build forces approval prompt so the consent screen always appears`() {
        val url = builder.build()

        assertTrue(url.contains("approval_prompt=force"))
    }

    @Test
    fun `build requests required scopes`() {
        val url = builder.build()

        assertTrue(url.contains("activity%3Aread"))
        assertTrue(url.contains("activity%3Awrite"))
        assertTrue(url.contains("scope=read"))
    }

    @Test
    fun `redirect URI constants compose to expected uri`() {
        assertEquals(
            "cyclingassistant://koflox.github.io/strava/callback",
            StravaAuthUrlBuilder.REDIRECT_URI,
        )
    }

    @Test
    fun `required scopes contains activity read and activity write`() {
        assertEquals(
            setOf("activity:read", "activity:write"),
            StravaAuthUrlBuilder.REQUIRED_SCOPES,
        )
    }
}
