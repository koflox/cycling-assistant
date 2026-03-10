package com.koflox.session.domain.usecase

import app.cash.turbine.test
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createTrackPoint
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObserveActiveSessionRouteUseCaseImplTest {

    companion object {
        private const val LAT_1 = 52.50
        private const val LON_1 = 13.40
        private const val LAT_2 = 52.51
        private const val LON_2 = 13.41
        private const val LAT_3 = 52.52
        private const val LON_3 = 13.42
        private const val TRACK_POINT_TIME_MS = 1000L
        private const val RESUMED_TIME_MS = 2000L
    }

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private lateinit var useCase: ObserveActiveSessionRouteUseCaseImpl

    @Before
    fun setup() {
        useCase = ObserveActiveSessionRouteUseCaseImpl(activeSessionUseCase)
    }

    @Test
    fun `null session emits null`() = runTest {
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(null)
        useCase.observe().test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `paused session sets isPaused true`() = runTest {
        val session = createSession(
            status = SessionStatus.PAUSED,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1),
                createTrackPoint(latitude = LAT_2, longitude = LON_2),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertTrue(snapshot.isPaused)
            assertTrue(snapshot.showGapToUserLocation)
            awaitComplete()
        }
    }

    @Test
    fun `running session with current track point sets isPaused false`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            lastResumedTimeMs = TRACK_POINT_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1, timestampMs = TRACK_POINT_TIME_MS),
                createTrackPoint(latitude = LAT_2, longitude = LON_2, timestampMs = RESUMED_TIME_MS),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertFalse(snapshot.isPaused)
            assertFalse(snapshot.showGapToUserLocation)
            awaitComplete()
        }
    }

    @Test
    fun `awaiting resume point shows gap to user location`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            lastResumedTimeMs = RESUMED_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1, timestampMs = TRACK_POINT_TIME_MS),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertFalse(snapshot.isPaused)
            assertTrue(snapshot.showGapToUserLocation)
            awaitComplete()
        }
    }

    @Test
    fun `two track points calculates bearing`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1),
                createTrackPoint(latitude = LAT_2, longitude = LON_2),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertNotNull(snapshot.lastBearingDegrees)
            awaitComplete()
        }
    }

    @Test
    fun `single track point has null bearing`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertNull(snapshot.lastBearingDegrees)
            awaitComplete()
        }
    }

    @Test
    fun `empty track points has null positions`() = runTest {
        val session = createSession(status = SessionStatus.RUNNING, trackPoints = emptyList())
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertNull(snapshot.firstTrackPointPosition)
            assertNull(snapshot.lastTrackPointPosition)
            assertNull(snapshot.lastBearingDegrees)
            awaitComplete()
        }
    }

    @Test
    fun `first and last positions match track points`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1),
                createTrackPoint(latitude = LAT_2, longitude = LON_2),
                createTrackPoint(latitude = LAT_3, longitude = LON_3),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            assertEquals(LAT_1, snapshot.firstTrackPointPosition!!.latitude, 0.0)
            assertEquals(LON_1, snapshot.firstTrackPointPosition.longitude, 0.0)
            assertEquals(LAT_3, snapshot.lastTrackPointPosition!!.latitude, 0.0)
            assertEquals(LON_3, snapshot.lastTrackPointPosition.longitude, 0.0)
            awaitComplete()
        }
    }

    @Test
    fun `bearing uses last two track points`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                createTrackPoint(latitude = LAT_1, longitude = LON_1),
                createTrackPoint(latitude = LAT_2, longitude = LON_2),
                createTrackPoint(latitude = LAT_2, longitude = LON_2 + 1.0),
            ),
        )
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(session)
        useCase.observe().test {
            val snapshot = awaitItem()!!
            val bearing = snapshot.lastBearingDegrees!!
            assertEquals(0f, bearing, 1f)
            awaitComplete()
        }
    }
}
