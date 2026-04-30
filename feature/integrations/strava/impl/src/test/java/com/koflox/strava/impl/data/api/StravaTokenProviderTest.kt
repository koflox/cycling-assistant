package com.koflox.strava.impl.data.api

import com.koflox.strava.impl.data.api.dto.AthleteResponse
import com.koflox.strava.impl.data.api.dto.TokenResponse
import com.koflox.strava.impl.data.mapper.TokenMapper
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StravaTokenProviderTest {

    companion object {
        private const val ACCESS = "access"
        private const val REFRESH = "refresh"
        private const val NEW_ACCESS = "new-access"
        private const val NEW_REFRESH = "new-refresh"
        private const val EXPIRES_AT_S = 1700000000L
        private const val ATHLETE_ID = 12345L
        private const val ATHLETE_NAME = "John Doe"
    }

    private val authApi: StravaAuthApi = mockk()
    private val tokenLocalDataSource: StravaTokenLocalDataSource = mockk(relaxed = true)
    private val tokenMapper: TokenMapper = mockk()

    private val provider = StravaTokenProvider(
        authApi = authApi,
        tokenLocalDataSource = tokenLocalDataSource,
        tokenMapper = tokenMapper,
    )

    @Test
    fun `loadTokens returns null when no entity is stored`() = runTest {
        coEvery { tokenLocalDataSource.get() } returns null

        assertNull(provider.loadTokens())
    }

    @Test
    fun `loadTokens maps stored entity to BearerTokens`() = runTest {
        coEvery { tokenLocalDataSource.get() } returns tokenEntity()

        val tokens = provider.loadTokens()

        assertNotNull(tokens)
        assertEquals(ACCESS, tokens?.accessToken)
        assertEquals(REFRESH, tokens?.refreshToken)
    }

    @Test
    fun `refreshTokens persists response with athleteName fallback and returns new tokens`() = runTest {
        val response = tokenResponse(athlete = null)
        val newEntity = tokenEntity(accessToken = NEW_ACCESS, refreshToken = NEW_REFRESH)
        coEvery { authApi.refreshToken(REFRESH) } returns response
        coEvery { tokenLocalDataSource.get() } returns tokenEntity()
        every { tokenMapper.toEntity(response, ATHLETE_NAME) } returns newEntity

        val result = provider.refreshTokens(REFRESH)

        assertNotNull(result)
        assertEquals(NEW_ACCESS, result?.accessToken)
        assertEquals(NEW_REFRESH, result?.refreshToken)
        coVerify { tokenLocalDataSource.upsert(newEntity) }
    }

    @Test
    fun `refreshTokens passes null fallback when no prior entity`() = runTest {
        val response = tokenResponse()
        val newEntity = tokenEntity(accessToken = NEW_ACCESS, refreshToken = NEW_REFRESH)
        coEvery { authApi.refreshToken(REFRESH) } returns response
        coEvery { tokenLocalDataSource.get() } returns null
        every { tokenMapper.toEntity(response, null) } returns newEntity

        val result = provider.refreshTokens(REFRESH)

        assertNotNull(result)
        assertEquals(NEW_ACCESS, result?.accessToken)
        assertEquals(NEW_REFRESH, result?.refreshToken)
    }

    @Test
    fun `refreshTokens deletes local token and returns null on API failure`() = runTest {
        coEvery { authApi.refreshToken(REFRESH) } throws RuntimeException("network down")

        val result = provider.refreshTokens(REFRESH)

        assertNull(result)
        coVerify { tokenLocalDataSource.delete() }
    }

    private fun tokenEntity(
        accessToken: String = ACCESS,
        refreshToken: String = REFRESH,
        athleteName: String = ATHLETE_NAME,
    ) = StravaTokenEntity(
        id = StravaTokenEntity.SINGLETON_ID,
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAtSeconds = EXPIRES_AT_S,
        athleteId = ATHLETE_ID,
        athleteName = athleteName,
    )

    private fun tokenResponse(athlete: AthleteResponse? = AthleteResponse(id = ATHLETE_ID)) = TokenResponse(
        accessToken = NEW_ACCESS,
        refreshToken = NEW_REFRESH,
        expiresAtSeconds = EXPIRES_AT_S,
        athlete = athlete,
    )
}
