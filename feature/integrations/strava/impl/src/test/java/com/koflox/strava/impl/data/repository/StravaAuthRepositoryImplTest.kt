package com.koflox.strava.impl.data.repository

import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StravaAuthRepositoryImplTest {

    companion object {
        private const val ATHLETE_ID = 12345L
        private const val ATHLETE_NAME = "John Doe"
        private const val ACCESS_TOKEN = "access"
        private const val REFRESH_TOKEN = "refresh"
        private const val EXPIRES_AT_S = 1700000000L
    }

    private val localDataSource: StravaTokenLocalDataSource = mockk()

    private val repository = StravaAuthRepositoryImpl(
        localDataSource = localDataSource,
    )

    @Test
    fun `observeAuthState emits LoggedOut when no token`() = runTest {
        every { localDataSource.observe() } returns flowOf(null)

        val result = repository.observeAuthState().first()

        assertEquals(StravaAuthState.LoggedOut, result)
    }

    @Test
    fun `observeAuthState emits LoggedIn when token present`() = runTest {
        every { localDataSource.observe() } returns flowOf(tokenEntity())

        val result = repository.observeAuthState().first()

        assertEquals(StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME), result)
    }

    @Test
    fun `getCurrentAuthState returns LoggedOut when no token`() = runTest {
        coEvery { localDataSource.get() } returns null

        val result = repository.getCurrentAuthState()

        assertEquals(StravaAuthState.LoggedOut, result)
    }

    @Test
    fun `getCurrentAuthState returns LoggedIn when token present`() = runTest {
        coEvery { localDataSource.get() } returns tokenEntity()

        val result = repository.getCurrentAuthState()

        assertEquals(StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME), result)
    }

    @Test
    fun `logout deletes token`() = runTest {
        coEvery { localDataSource.delete() } returns Unit

        repository.logout()

        coVerify { localDataSource.delete() }
    }

    private fun tokenEntity() = StravaTokenEntity(
        id = StravaTokenEntity.SINGLETON_ID,
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN,
        expiresAtSeconds = EXPIRES_AT_S,
        athleteId = ATHLETE_ID,
        athleteName = ATHLETE_NAME,
    )
}
