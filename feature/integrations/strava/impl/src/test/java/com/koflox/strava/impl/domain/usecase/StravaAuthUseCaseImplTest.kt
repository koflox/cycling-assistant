package com.koflox.strava.impl.domain.usecase

import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.data.api.dto.AthleteResponse
import com.koflox.strava.impl.data.api.dto.TokenResponse
import com.koflox.strava.impl.data.mapper.TokenMapper
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import com.koflox.strava.impl.data.source.remote.StravaAuthRemoteDataSource
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StravaAuthUseCaseImplTest {

    companion object {
        private const val CODE = "auth-code-123"
        private const val ACCESS_TOKEN = "access"
        private const val REFRESH_TOKEN = "refresh"
        private const val EXPIRES_AT_S = 1700000000L
        private const val ATHLETE_ID = 12345L
        private const val ATHLETE_NAME = "John Doe"
    }

    private val authRepository: StravaAuthRepository = mockk()
    private val remoteDataSource: StravaAuthRemoteDataSource = mockk()
    private val tokenLocalDataSource: StravaTokenLocalDataSource = mockk()
    private val tokenMapper: TokenMapper = mockk()

    private val useCase = StravaAuthUseCaseImpl(
        authRepository = authRepository,
        remoteDataSource = remoteDataSource,
        tokenLocalDataSource = tokenLocalDataSource,
        tokenMapper = tokenMapper,
    )

    @Test
    fun `login exchanges code and persists mapped entity`() = runTest {
        val response = tokenResponse()
        val entity = tokenEntity()
        coEvery { remoteDataSource.exchangeAuthorizationCode(CODE) } returns response
        every { tokenMapper.toEntity(response) } returns entity
        coEvery { tokenLocalDataSource.upsert(entity) } returns Unit

        val result = useCase.login(CODE)

        assertTrue(result.isSuccess)
        coVerify { tokenLocalDataSource.upsert(entity) }
    }

    @Test
    fun `login returns failure when remote fails`() = runTest {
        val error = RuntimeException("network down")
        coEvery { remoteDataSource.exchangeAuthorizationCode(CODE) } throws error

        val result = useCase.login(CODE)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `logout delegates to repository`() = runTest {
        coEvery { authRepository.logout() } returns Unit

        val result = useCase.logout()

        assertTrue(result.isSuccess)
        coVerify { authRepository.logout() }
    }

    @Test
    fun `observeAuthState delegates to repository`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME))

        val result = useCase.observeAuthState().first()

        assertEquals(StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME), result)
    }

    private fun tokenResponse() = TokenResponse(
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN,
        expiresAtSeconds = EXPIRES_AT_S,
        athlete = AthleteResponse(id = ATHLETE_ID, firstname = "John", lastname = "Doe"),
    )

    private fun tokenEntity() = StravaTokenEntity(
        id = StravaTokenEntity.SINGLETON_ID,
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN,
        expiresAtSeconds = EXPIRES_AT_S,
        athleteId = ATHLETE_ID,
        athleteName = ATHLETE_NAME,
    )
}
