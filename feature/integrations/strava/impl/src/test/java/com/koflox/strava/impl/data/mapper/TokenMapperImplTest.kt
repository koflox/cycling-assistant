package com.koflox.strava.impl.data.mapper

import com.koflox.strava.impl.data.api.dto.AthleteResponse
import com.koflox.strava.impl.data.api.dto.TokenResponse
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenMapperImplTest {

    companion object {
        private const val ACCESS_TOKEN = "access"
        private const val REFRESH_TOKEN = "refresh"
        private const val EXPIRES_AT_S = 1700000000L
        private const val ATHLETE_ID = 12345L
    }

    private val mapper: TokenMapper = TokenMapperImpl()

    @Test
    fun `toEntity combines firstname and lastname`() {
        val response = response(athlete = athlete(firstname = "John", lastname = "Doe"))

        val entity = mapper.toEntity(response)

        assertEquals("John Doe", entity.athleteName)
        assertEquals(ATHLETE_ID, entity.athleteId)
    }

    @Test
    fun `toEntity uses firstname when lastname missing`() {
        val response = response(athlete = athlete(firstname = "John", lastname = null))

        val entity = mapper.toEntity(response)

        assertEquals("John", entity.athleteName)
    }

    @Test
    fun `toEntity falls back to username when names blank`() {
        val response = response(athlete = athlete(firstname = "", lastname = "", username = "johnny"))

        val entity = mapper.toEntity(response)

        assertEquals("johnny", entity.athleteName)
    }

    @Test
    fun `toEntity uses fallback when athlete is null`() {
        val response = response(athlete = null)

        val entity = mapper.toEntity(response, fallbackAthleteName = "Cached Name")

        assertEquals("Cached Name", entity.athleteName)
        assertEquals(0L, entity.athleteId)
    }

    @Test
    fun `toEntity returns empty athleteName when nothing available`() {
        val response = response(athlete = null)

        val entity = mapper.toEntity(response)

        assertEquals("", entity.athleteName)
    }

    @Test
    fun `toEntity preserves token fields`() {
        val response = response(athlete = athlete(firstname = "John"))

        val entity = mapper.toEntity(response)

        assertEquals(StravaTokenEntity.SINGLETON_ID, entity.id)
        assertEquals(ACCESS_TOKEN, entity.accessToken)
        assertEquals(REFRESH_TOKEN, entity.refreshToken)
        assertEquals(EXPIRES_AT_S, entity.expiresAtSeconds)
    }

    private fun response(athlete: AthleteResponse?) = TokenResponse(
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN,
        expiresAtSeconds = EXPIRES_AT_S,
        athlete = athlete,
    )

    private fun athlete(
        id: Long = ATHLETE_ID,
        firstname: String? = null,
        lastname: String? = null,
        username: String? = null,
    ) = AthleteResponse(
        id = id,
        firstname = firstname,
        lastname = lastname,
        username = username,
    )
}
