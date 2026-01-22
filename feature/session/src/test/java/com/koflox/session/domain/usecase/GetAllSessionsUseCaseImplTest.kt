package com.koflox.session.domain.usecase

import app.cash.turbine.test
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetAllSessionsUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: GetAllSessionsUseCaseImpl

    @Before
    fun setup() {
        useCase = GetAllSessionsUseCaseImpl(sessionRepository)
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeAllSessions delegates to repository`() = runTest {
        every { sessionRepository.observeAllSessions() } returns flowOf(emptyList())

        useCase.observeAllSessions()

        verify(exactly = 1) { sessionRepository.observeAllSessions() }
    }

    @Test
    fun `observeAllSessions returns flow from repository`() = runTest {
        val sessions = listOf(createSession())
        val flow = flowOf(sessions)
        every { sessionRepository.observeAllSessions() } returns flow

        val result = useCase.observeAllSessions()

        assertEquals(flow, result)
    }

    @Test
    fun `observeAllSessions emits sessions`() = runTest {
        val sessions = listOf(createSession())
        every { sessionRepository.observeAllSessions() } returns flowOf(sessions)

        useCase.observeAllSessions().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(SESSION_ID, result[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAllSessions emits empty list when no sessions`() = runTest {
        every { sessionRepository.observeAllSessions() } returns flowOf(emptyList())

        useCase.observeAllSessions().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAllSessions emits multiple sessions`() = runTest {
        val sessions = listOf(
            createSession(id = "session-1"),
            createSession(id = "session-2"),
            createSession(id = "session-3"),
        )
        every { sessionRepository.observeAllSessions() } returns flowOf(sessions)

        useCase.observeAllSessions().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("session-1", result[0].id)
            assertEquals("session-2", result[1].id)
            assertEquals("session-3", result[2].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAllSessions emits updates`() = runTest {
        val initialSessions = emptyList<Session>()
        val updatedSessions = listOf(createSession())
        every { sessionRepository.observeAllSessions() } returns flowOf(initialSessions, updatedSessions)

        useCase.observeAllSessions().test {
            assertTrue(awaitItem().isEmpty())
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
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
