package com.koflox.session.domain.usecase

import com.koflox.altitude.AltitudeCalculator
import com.koflox.distance.DistanceCalculator
import com.koflox.location.LocationValidator
import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createTrackPoint
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        private const val NEW_TIMESTAMP_MS = 1108000L
        private const val DISTANCE_KM = 1.5
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
        private const val TIME_DIFF_MS = NEW_TIMESTAMP_MS - START_TIME_MS
        private const val EXPECTED_SPEED_KMH = (DISTANCE_KM / TIME_DIFF_MS) * MILLISECONDS_PER_HOUR
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private val altitudeCalculator: AltitudeCalculator = mockk()
    private val locationValidator: LocationValidator = mockk()
    private lateinit var useCase: UpdateSessionLocationUseCaseImpl

    @Before
    fun setup() {
        every { altitudeCalculator.calculateGain(any(), any()) } returns 0.0
        every { locationValidator.isAccuracyValid(any()) } returns true
        useCase = UpdateSessionLocationUseCaseImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            activeSessionUseCase = activeSessionUseCase,
            sessionRepository = sessionRepository,
            distanceCalculator = distanceCalculator,
            altitudeCalculator = altitudeCalculator,
            locationValidator = locationValidator,
        )
    }

    @Test
    fun `update gets active session`() = runTest {
        val session = createTestSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        coVerify { activeSessionUseCase.getActiveSession() }
    }

    @Test
    fun `update does nothing for paused session`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update does nothing for completed session`() = runTest {
        val session = createTestSession(status = SessionStatus.COMPLETED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update does nothing for invalid accuracy`() = runTest {
        every { locationValidator.isAccuracyValid(any()) } returns false

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        coVerify(exactly = 0) { activeSessionUseCase.getActiveSession() }
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update calculates distance from previous track point`() = runTest {
        val session = createTestSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(START_LAT, START_LONG, NEW_LAT, NEW_LONG) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        coVerify { distanceCalculator.calculateKm(START_LAT, START_LONG, NEW_LAT, NEW_LONG) }
    }

    @Test
    fun `update adds new track point`() = runTest {
        val session = createTestSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(2, sessionSlot.captured.trackPoints.size)
        val newTrackPoint = sessionSlot.captured.trackPoints[1]
        assertEquals(NEW_LAT, newTrackPoint.latitude, 0.0)
        assertEquals(NEW_LONG, newTrackPoint.longitude, 0.0)
        assertEquals(NEW_TIMESTAMP_MS, newTrackPoint.timestampMs)
    }

    @Test
    fun `update calculates speed for new track point`() = runTest {
        val session = createTestSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        val newTrackPoint = sessionSlot.captured.trackPoints[1]
        assertEquals(EXPECTED_SPEED_KMH, newTrackPoint.speedKmh, 0.0)
    }

    @Test
    fun `update increases total distance`() = runTest {
        val session = createTestSession(traveledDistanceKm = 5.0)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(5.0 + DISTANCE_KM, sessionSlot.captured.traveledDistanceKm, 0.0)
    }

    @Test
    fun `update updates top speed when current speed is higher`() = runTest {
        val initialTopSpeed = 10.0
        val session = createTestSession(topSpeedKmh = initialTopSpeed)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(EXPECTED_SPEED_KMH, sessionSlot.captured.topSpeedKmh, 0.0)
    }

    @Test
    fun `update preserves top speed when current speed is lower`() = runTest {
        val smallDistanceKm = 0.005
        val session = createTestSession(topSpeedKmh = 100.0)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns smallDistanceKm
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(100.0, sessionSlot.captured.topSpeedKmh, 0.0)
    }

    @Test
    fun `update updates elapsed time`() = runTest {
        val session = createTestSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(TIME_DIFF_MS, sessionSlot.captured.elapsedTimeMs)
    }

    @Test
    fun `update updates lastResumedTimeMs to new timestamp`() = runTest {
        val session = createTestSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(NEW_TIMESTAMP_MS, sessionSlot.captured.lastResumedTimeMs)
    }

    @Test
    fun `update calculates average speed`() = runTest {
        val session = createTestSession()
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(EXPECTED_SPEED_KMH, sessionSlot.captured.averageSpeedKmh, 0.0)
    }

    @Test
    fun `update saves session to repository`() = runTest {
        val session = createTestSession()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        coVerify { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `update handles empty track points gracefully`() = runTest {
        val session = createTestSession().copy(trackPoints = emptyList())
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        assertEquals(1, sessionSlot.captured.trackPoints.size)
        assertEquals(0.0, sessionSlot.captured.trackPoints[0].speedKmh, 0.0)
    }

    @Test
    fun `update handles zero time difference`() = runTest {
        val trackPoint = createTrackPoint(
            latitude = START_LAT,
            longitude = START_LONG,
            timestampMs = NEW_TIMESTAMP_MS,
        )
        val session = createTestSession().copy(trackPoints = listOf(trackPoint))
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_KM
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.update(createNewLocation(), NEW_TIMESTAMP_MS)

        val newTrackPoint = sessionSlot.captured.trackPoints[1]
        assertEquals(0.0, newTrackPoint.speedKmh, 0.0)
    }

    private fun createNewLocation() = Location(
        latitude = NEW_LAT,
        longitude = NEW_LONG,
    )

    private fun createTestSession(
        id: String = SESSION_ID,
        status: SessionStatus = SessionStatus.RUNNING,
        traveledDistanceKm: Double = 0.0,
        topSpeedKmh: Double = 0.0,
    ) = createSession(
        id = id,
        lastResumedTimeMs = START_TIME_MS,
        traveledDistanceKm = traveledDistanceKm,
        topSpeedKmh = topSpeedKmh,
        status = status,
        trackPoints = listOf(
            createTrackPoint(
                latitude = START_LAT,
                longitude = START_LONG,
                timestampMs = START_TIME_MS,
            ),
        ),
    )
}
