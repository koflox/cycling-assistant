package com.koflox.destinationsession.bridge.impl.usecase

import app.cash.turbine.test
import com.koflox.location.model.Location
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.session.domain.usecase.ObserveActiveSessionRouteUseCase
import com.koflox.session.domain.usecase.SessionRouteSnapshot
import com.koflox.session.presentation.route.RouteDisplayData
import com.koflox.session.testutil.createSession
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CyclingSessionUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_ID = "dest-456"
        private const val START_LAT = 35.0
        private const val START_LNG = 139.0
        private const val END_LAT = 35.1
        private const val END_LNG = 139.1
        private const val BEARING_DEGREES = 45f
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val observeActiveSessionRouteUseCase: ObserveActiveSessionRouteUseCase = mockk()
    private lateinit var useCase: CyclingSessionUseCaseImpl

    @Before
    fun setup() {
        useCase = CyclingSessionUseCaseImpl(
            activeSessionUseCase = activeSessionUseCase,
            observeActiveSessionRouteUseCase = observeActiveSessionRouteUseCase,
        )
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
        val session = createSession(id = SESSION_ID, destinationId = DESTINATION_ID)
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
        val session = createSession(id = SESSION_ID, destinationId = customDestinationId)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.getActiveSessionDestination()

        assertEquals(customDestinationId, result?.id)
    }

    @Test
    fun `getActiveSessionDestination returns null for free roam session`() = runTest {
        val session = createSession(id = SESSION_ID, destinationId = null)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.getActiveSessionDestination()

        assertNull(result)
    }

    @Test
    fun `observeActiveSessionRoute emits null when snapshot is null`() = runTest {
        every { observeActiveSessionRouteUseCase.observe() } returns flowOf(null)

        useCase.observeActiveSessionRoute().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSessionRoute maps start and last positions from snapshot`() = runTest {
        val startPosition = Location(latitude = START_LAT, longitude = START_LNG)
        val lastPosition = Location(latitude = END_LAT, longitude = END_LNG)
        val snapshot = SessionRouteSnapshot(
            routeDisplayData = RouteDisplayData.EMPTY,
            isPaused = false,
            showGapToUserLocation = false,
            firstTrackPointPosition = startPosition,
            lastTrackPointPosition = lastPosition,
            lastBearingDegrees = BEARING_DEGREES,
        )
        every { observeActiveSessionRouteUseCase.observe() } returns flowOf(snapshot)

        useCase.observeActiveSessionRoute().test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(startPosition, result!!.startPosition)
            assertEquals(lastPosition, result.lastPosition)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSessionRoute maps lastBearingDegrees from snapshot`() = runTest {
        val snapshot = SessionRouteSnapshot(
            routeDisplayData = RouteDisplayData.EMPTY,
            isPaused = false,
            showGapToUserLocation = false,
            firstTrackPointPosition = Location(latitude = START_LAT, longitude = START_LNG),
            lastTrackPointPosition = Location(latitude = END_LAT, longitude = END_LNG),
            lastBearingDegrees = BEARING_DEGREES,
        )
        every { observeActiveSessionRouteUseCase.observe() } returns flowOf(snapshot)

        useCase.observeActiveSessionRoute().test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(BEARING_DEGREES, result!!.lastBearingDegrees)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSessionRoute maps null positions when snapshot has no track points`() = runTest {
        val snapshot = SessionRouteSnapshot(
            routeDisplayData = RouteDisplayData.EMPTY,
            isPaused = false,
            showGapToUserLocation = false,
            firstTrackPointPosition = null,
            lastTrackPointPosition = null,
            lastBearingDegrees = null,
        )
        every { observeActiveSessionRouteUseCase.observe() } returns flowOf(snapshot)

        useCase.observeActiveSessionRoute().test {
            val result = awaitItem()
            assertNotNull(result)
            assertNull(result!!.startPosition)
            assertNull(result.lastPosition)
            assertNull(result.lastBearingDegrees)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSessionRoute maps isPaused from snapshot`() = runTest {
        val snapshot = SessionRouteSnapshot(
            routeDisplayData = RouteDisplayData.EMPTY,
            isPaused = true,
            showGapToUserLocation = true,
            firstTrackPointPosition = null,
            lastTrackPointPosition = null,
            lastBearingDegrees = null,
        )
        every { observeActiveSessionRouteUseCase.observe() } returns flowOf(snapshot)

        useCase.observeActiveSessionRoute().test {
            val result = awaitItem()
            assertNotNull(result)
            assertTrue(result!!.isPaused)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSessionRoute maps showGapToUserLocation from snapshot`() = runTest {
        val snapshot = SessionRouteSnapshot(
            routeDisplayData = RouteDisplayData.EMPTY,
            isPaused = false,
            showGapToUserLocation = true,
            firstTrackPointPosition = null,
            lastTrackPointPosition = null,
            lastBearingDegrees = null,
        )
        every { observeActiveSessionRouteUseCase.observe() } returns flowOf(snapshot)

        useCase.observeActiveSessionRoute().test {
            val result = awaitItem()
            assertNotNull(result)
            assertTrue(result!!.showGapToUserLocation)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
