package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.testutil.createSession
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UpdateSessionStatusUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val START_TIME_MS = 1000000L
        private const val LAST_RESUMED_TIME_MS = 1500000L
        private const val ELAPSED_TIME_MS = 300000L
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
        private const val TRAVELED_DISTANCE_KM = 10.0
        private const val AVERAGE_SPEED_KMH = 25.0
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: UpdateSessionStatusUseCaseImpl

    @Before
    fun setup() {
        useCase = UpdateSessionStatusUseCaseImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            mutex = Mutex(),
            activeSessionUseCase = activeSessionUseCase,
            sessionRepository = sessionRepository,
        )
    }

    @Test
    fun `pause gets active session`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.pause()

        coVerify { activeSessionUseCase.getActiveSession() }
    }

    @Test
    fun `pause updates status to paused`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.pause()

        assertEquals(SessionStatus.PAUSED, sessionSlot.captured.status)
    }

    @Test
    fun `pause updates elapsed time`() = runTest {
        val session = createTestSession(
            status = SessionStatus.RUNNING,
            elapsedTimeMs = ELAPSED_TIME_MS,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.pause()

        assertTrue(sessionSlot.captured.elapsedTimeMs > ELAPSED_TIME_MS)
    }

    @Test
    fun `pause saves session to repository`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.pause()

        coVerify { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `pause returns success on successful save`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        val result = useCase.pause()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `pause returns failure when no active session`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.pause()

        assertTrue(result.isFailure)
    }

    @Test
    fun `pause does nothing when session is paused`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.pause()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `pause does nothing when session is completed`() = runTest {
        val session = createTestSession(status = SessionStatus.COMPLETED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.pause()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `resume gets active session`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.resume()

        coVerify { activeSessionUseCase.getActiveSession() }
    }

    @Test
    fun `resume updates status to running`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.resume()

        assertEquals(SessionStatus.RUNNING, sessionSlot.captured.status)
    }

    @Test
    fun `resume updates lastResumedTimeMs`() = runTest {
        val session = createTestSession(
            status = SessionStatus.PAUSED,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.resume()

        assertTrue(sessionSlot.captured.lastResumedTimeMs > LAST_RESUMED_TIME_MS)
    }

    @Test
    fun `resume saves session to repository`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.resume()

        coVerify { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `resume returns success on successful save`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        val result = useCase.resume()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `resume returns failure when no active session`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.resume()

        assertTrue(result.isFailure)
    }

    @Test
    fun `resume does nothing when session is running`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.resume()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `resume does nothing when session is completed`() = runTest {
        val session = createTestSession(status = SessionStatus.COMPLETED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.resume()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `stop gets active session`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.stop()

        coVerify { activeSessionUseCase.getActiveSession() }
    }

    @Test
    fun `stop updates status to completed`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertEquals(SessionStatus.COMPLETED, sessionSlot.captured.status)
    }

    @Test
    fun `stop sets endTimeMs`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertNotNull(sessionSlot.captured.endTimeMs)
    }

    @Test
    fun `stop calculates final elapsed time for running session`() = runTest {
        val session = createTestSession(
            status = SessionStatus.RUNNING,
            elapsedTimeMs = ELAPSED_TIME_MS,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertTrue(sessionSlot.captured.elapsedTimeMs > ELAPSED_TIME_MS)
    }

    @Test
    fun `stop preserves elapsed time for paused session`() = runTest {
        val session = createTestSession(
            status = SessionStatus.PAUSED,
            elapsedTimeMs = ELAPSED_TIME_MS,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertEquals(ELAPSED_TIME_MS, sessionSlot.captured.elapsedTimeMs)
    }

    @Test
    fun `stop saves session to repository`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.stop()

        coVerify { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `stop returns success on successful save`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        val result = useCase.stop()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `stop returns failure when no active session`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.stop()

        assertTrue(result.isFailure)
    }

    @Test
    fun `stop does nothing when session is completed`() = runTest {
        val session = createTestSession(status = SessionStatus.COMPLETED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.stop()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `pause recalculates average speed`() = runTest {
        val session = createTestSession(
            status = SessionStatus.RUNNING,
            elapsedTimeMs = ELAPSED_TIME_MS,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
            traveledDistanceKm = TRAVELED_DISTANCE_KM,
            averageSpeedKmh = AVERAGE_SPEED_KMH,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.pause()

        assertTrue(sessionSlot.captured.averageSpeedKmh < AVERAGE_SPEED_KMH)
    }

    @Test
    fun `stop recalculates average speed for running session`() = runTest {
        val session = createTestSession(
            status = SessionStatus.RUNNING,
            elapsedTimeMs = ELAPSED_TIME_MS,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
            traveledDistanceKm = TRAVELED_DISTANCE_KM,
            averageSpeedKmh = AVERAGE_SPEED_KMH,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertTrue(sessionSlot.captured.averageSpeedKmh < AVERAGE_SPEED_KMH)
    }

    @Test
    fun `stop preserves average speed for paused session`() = runTest {
        val consistentAvgSpeed = (TRAVELED_DISTANCE_KM / ELAPSED_TIME_MS) * MILLISECONDS_PER_HOUR
        val session = createTestSession(
            status = SessionStatus.PAUSED,
            elapsedTimeMs = ELAPSED_TIME_MS,
            traveledDistanceKm = TRAVELED_DISTANCE_KM,
            averageSpeedKmh = consistentAvgSpeed,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertEquals(consistentAvgSpeed, sessionSlot.captured.averageSpeedKmh, 0.001)
    }

    @Test
    fun `onServiceRestart updates lastResumedTimeMs for running session`() = runTest {
        val session = createTestSession(
            status = SessionStatus.RUNNING,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.onServiceRestart()

        assertTrue(sessionSlot.captured.lastResumedTimeMs > LAST_RESUMED_TIME_MS)
    }

    @Test
    fun `onServiceRestart preserves elapsedTimeMs`() = runTest {
        val session = createTestSession(
            status = SessionStatus.RUNNING,
            elapsedTimeMs = ELAPSED_TIME_MS,
            lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.onServiceRestart()

        assertEquals(ELAPSED_TIME_MS, sessionSlot.captured.elapsedTimeMs)
    }

    @Test
    fun `onServiceRestart preserves running status`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.onServiceRestart()

        assertEquals(SessionStatus.RUNNING, sessionSlot.captured.status)
    }

    @Test
    fun `onServiceRestart does nothing when session is paused`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.onServiceRestart()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `onServiceRestart returns failure when no active session`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.onServiceRestart()

        assertTrue(result.isFailure)
    }

    @Test
    fun `stop ensures top speed is at least average speed`() = runTest {
        val lowTopSpeed = 5.0
        val session = createTestSession(
            status = SessionStatus.PAUSED,
            elapsedTimeMs = ELAPSED_TIME_MS,
            traveledDistanceKm = TRAVELED_DISTANCE_KM,
            averageSpeedKmh = AVERAGE_SPEED_KMH,
            topSpeedKmh = lowTopSpeed,
        )
        val sessionSlot = slot<Session>()
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.stop()

        assertTrue(sessionSlot.captured.topSpeedKmh >= sessionSlot.captured.averageSpeedKmh)
    }

    private fun createTestSession(
        id: String = SESSION_ID,
        status: SessionStatus = SessionStatus.RUNNING,
        elapsedTimeMs: Long = 0L,
        lastResumedTimeMs: Long = START_TIME_MS,
        traveledDistanceKm: Double = 0.0,
        averageSpeedKmh: Double = 0.0,
        topSpeedKmh: Double = 0.0,
    ) = createSession(
        id = id,
        lastResumedTimeMs = lastResumedTimeMs,
        elapsedTimeMs = elapsedTimeMs,
        traveledDistanceKm = traveledDistanceKm,
        averageSpeedKmh = averageSpeedKmh,
        topSpeedKmh = topSpeedKmh,
        status = status,
    )
}
