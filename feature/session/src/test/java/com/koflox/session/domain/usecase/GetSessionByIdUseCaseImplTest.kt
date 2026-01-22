package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetSessionByIdUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: GetSessionByIdUseCaseImpl

    @Before
    fun setup() {
        useCase = GetSessionByIdUseCaseImpl(sessionRepository)
    }

    @Test
    fun `getSession delegates to repository`() = runTest {
        val session = createSession()
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        useCase.getSession(SESSION_ID)

        coVerify(exactly = 1) { sessionRepository.getSession(SESSION_ID) }
    }

    @Test
    fun `getSession returns success when session found`() = runTest {
        val session = createSession()
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.getSession(SESSION_ID)

        assertTrue(result.isSuccess)
        assertEquals(session, result.getOrNull())
    }

    @Test
    fun `getSession returns failure when session not found`() = runTest {
        val exception = NoSuchElementException("Session not found")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.failure(exception)

        val result = useCase.getSession(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getSession returns failure on repository error`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.failure(exception)

        val result = useCase.getSession(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getSession passes correct session id to repository`() = runTest {
        val customId = "custom-session-id"
        val session = createSession(id = customId)
        coEvery { sessionRepository.getSession(customId) } returns Result.success(session)

        useCase.getSession(customId)

        coVerify { sessionRepository.getSession(customId) }
    }

    @Test
    fun `getSession returns session with correct id`() = runTest {
        val session = createSession()
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.getSession(SESSION_ID)

        assertEquals(SESSION_ID, result.getOrNull()?.id)
    }

    private fun createSession(
        id: String = SESSION_ID,
    ) = Session(
        id = id,
        destinationId = "dest-456",
        destinationName = "Test Destination",
        destinationLatitude = 52.52,
        destinationLongitude = 13.405,
        startLatitude = 52.50,
        startLongitude = 13.40,
        startTimeMs = 1000000L,
        lastResumedTimeMs = 1000000L,
        endTimeMs = 2000000L,
        elapsedTimeMs = 900000L,
        traveledDistanceKm = 5.5,
        averageSpeedKmh = 22.0,
        topSpeedKmh = 35.0,
        status = SessionStatus.COMPLETED,
        trackPoints = listOf(
            TrackPoint(
                latitude = 52.51,
                longitude = 13.41,
                timestampMs = 1500000L,
                speedKmh = 25.0,
            ),
        ),
    )
}
