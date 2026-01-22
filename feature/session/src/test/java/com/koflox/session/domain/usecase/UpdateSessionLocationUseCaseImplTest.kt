package com.koflox.session.domain.usecase

import com.koflox.distance.DistanceCalculator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UpdateSessionLocationUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val START_TIME_MS = 1000000L
        private const val START_LAT = 52.50
        private const val START_LONG = 13.40
        private const val NEW_LAT = 52.51
        private const val NEW_LONG = 13.41
        private const val NEW_TIMESTAMP_MS = 1003600L
        private const val DISTANCE_KM = 1.5
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private lateinit var useCase: UpdateSessionLocationUseCaseImpl

    @Before
    fun setup() {
        useCase = UpdateSessionLocationUseCaseImpl(
            activeSessionUseCase = activeSessionUseCase,
            sessionRepository = sessionRepository,
            distanceCalculator = distanceCalculator,
        )
    }

    @Test
    fun `update gets active session`() = runTest {
        val session = createSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        coVerify { activeSessionUseCase.getActiveSession() }
    }

    @Test
    fun `update does nothing for paused session`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update does nothing for completed session`() = runTest {
        val session = createSession(status = SessionStatus.COMPLETED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update calculates distance from previous track point`() = runTest {
        val session = createSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(START_LAT, START_LONG, NEW_LAT, NEW_LONG) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        coVerify { distanceCalculator.calculateKm(START_LAT, START_LONG, NEW_LAT, NEW_LONG) }
    }

    @Test
    fun `update adds new track point`() = runTest {
        val session = createSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertEquals(2, sessionSlot.captured.trackPoints.size)
        val newTrackPoint = sessionSlot.captured.trackPoints[1]
        assertEquals(NEW_LAT, newTrackPoint.latitude, 0.0)
        assertEquals(NEW_LONG, newTrackPoint.longitude, 0.0)
        assertEquals(NEW_TIMESTAMP_MS, newTrackPoint.timestampMs)
    }

    @Test
    fun `update calculates speed for new track point`() = runTest {
        val session = createSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        val newTrackPoint = sessionSlot.captured.trackPoints[1]
        assertTrue(newTrackPoint.speedKmh > 0)
    }

    @Test
    fun `update increases total distance`() = runTest {
        val session = createSession(traveledDistanceKm = 5.0)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertEquals(5.0 + DISTANCE_KM, sessionSlot.captured.traveledDistanceKm, 0.0)
    }

    @Test
    fun `update updates top speed when current speed is higher`() = runTest {
        val session = createSession(topSpeedKmh = 10.0)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertTrue(sessionSlot.captured.topSpeedKmh >= 10.0)
    }

    @Test
    fun `update preserves top speed when current speed is lower`() = runTest {
        val session = createSession(topSpeedKmh = 100.0)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.001
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertEquals(100.0, sessionSlot.captured.topSpeedKmh, 0.0)
    }

    @Test
    fun `update updates elapsed time`() = runTest {
        val session = createSession(elapsedTimeMs = 1000L)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertTrue(sessionSlot.captured.elapsedTimeMs > 1000L)
    }

    @Test
    fun `update updates lastResumedTimeMs to new timestamp`() = runTest {
        val session = createSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertEquals(NEW_TIMESTAMP_MS, sessionSlot.captured.lastResumedTimeMs)
    }

    @Test
    fun `update calculates average speed`() = runTest {
        val session = createSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertTrue(sessionSlot.captured.averageSpeedKmh >= 0.0)
    }

    @Test
    fun `update saves session to repository`() = runTest {
        val session = createSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        coVerify { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update handles empty track points gracefully`() = runTest {
        val session = createSession().copy(trackPoints = emptyList())
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        assertEquals(1, sessionSlot.captured.trackPoints.size)
        assertEquals(0.0, sessionSlot.captured.trackPoints[0].speedKmh, 0.0)
    }

    @Test
    fun `update handles zero time difference`() = runTest {
        val trackPoint = TrackPoint(
            latitude = START_LAT,
            longitude = START_LONG,
            timestampMs = NEW_TIMESTAMP_MS,
            speedKmh = 0.0,
        )
        val session = createSession().copy(trackPoints = listOf(trackPoint))
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(NEW_LAT, NEW_LONG, NEW_TIMESTAMP_MS)

        val newTrackPoint = sessionSlot.captured.trackPoints[1]
        assertEquals(0.0, newTrackPoint.speedKmh, 0.0)
    }

    private fun createSession(
        id: String = SESSION_ID,
        status: SessionStatus = SessionStatus.RUNNING,
        traveledDistanceKm: Double = 0.0,
        topSpeedKmh: Double = 0.0,
        elapsedTimeMs: Long = 0L,
    ) = Session(
        id = id,
        destinationId = "dest-456",
        destinationName = "Test Destination",
        destinationLatitude = 52.52,
        destinationLongitude = 13.405,
        startLatitude = START_LAT,
        startLongitude = START_LONG,
        startTimeMs = START_TIME_MS,
        lastResumedTimeMs = START_TIME_MS,
        endTimeMs = null,
        elapsedTimeMs = elapsedTimeMs,
        traveledDistanceKm = traveledDistanceKm,
        averageSpeedKmh = 0.0,
        topSpeedKmh = topSpeedKmh,
        status = status,
        trackPoints = listOf(
            TrackPoint(
                latitude = START_LAT,
                longitude = START_LONG,
                timestampMs = START_TIME_MS,
                speedKmh = 0.0,
            ),
        ),
    )
}
