package com.koflox.destinationsession.bridge.impl.usecase

import app.cash.turbine.test
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CyclingSessionUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_ID = "dest-456"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private lateinit var useCase: CyclingSessionUseCaseImpl

    @Before
    fun setup() {
        useCase = CyclingSessionUseCaseImpl(activeSessionUseCase)
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeHasActiveSession delegates to activeSessionUseCase`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(false)

        useCase.observeHasActiveSession()

        verify(exactly = 1) { activeSessionUseCase.hasActiveSession() }
    }

    @Test
    fun `observeHasActiveSession returns true when session exists`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(true)

        useCase.observeHasActiveSession().test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeHasActiveSession returns false when no session`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(false)

        useCase.observeHasActiveSession().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeHasActiveSession emits updates`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(false, true, false)

        useCase.observeHasActiveSession().test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActiveSessionDestination returns destination when session exists`() = runTest {
        val session = createSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.getActiveSessionDestination()

        assertEquals(DESTINATION_ID, result?.id)
    }

    @Test
    fun `getActiveSessionDestination returns null when no active session`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.getActiveSessionDestination()

        assertNull(result)
    }

    @Test
    fun `getActiveSessionDestination handles NoActiveSessionException gracefully`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.getActiveSessionDestination()

        assertNull(result)
    }

    @Test
    fun `getActiveSessionDestination returns correct destination id`() = runTest {
        val customDestinationId = "custom-dest-id"
        val session = createSession(destinationId = customDestinationId)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.getActiveSessionDestination()

        assertEquals(customDestinationId, result?.id)
    }

    private fun createSession(
        id: String = SESSION_ID,
        destinationId: String = DESTINATION_ID,
    ) = Session(
        id = id,
        destinationId = destinationId,
        destinationName = "Test Destination",
        destinationLatitude = 52.52,
        destinationLongitude = 13.405,
        startLatitude = 52.50,
        startLongitude = 13.40,
        startTimeMs = 1000000L,
        lastResumedTimeMs = 1000000L,
        endTimeMs = null,
        elapsedTimeMs = 0L,
        traveledDistanceKm = 0.0,
        averageSpeedKmh = 0.0,
        topSpeedKmh = 0.0,
        status = SessionStatus.RUNNING,
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
