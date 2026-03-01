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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UpdateSessionPowerUseCaseImplTest {

    companion object {
        private const val POWER_WATTS = 200
        private const val TIMESTAMP_1_MS = 10_000L
        private const val TIMESTAMP_2_MS = 11_000L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val mutex = Mutex()
    private lateinit var useCase: UpdateSessionPowerUseCaseImpl

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)
    }

    private fun createUseCase() = UpdateSessionPowerUseCaseImpl(
        dispatcherDefault = mainDispatcherRule.testDispatcher,
        mutex = mutex,
        activeSessionUseCase = activeSessionUseCase,
        sessionRepository = sessionRepository,
    )

    @Test
    fun `first reading sets totalPowerReadings to 1 and zero energy delta`() = runTest {
        val session = createSession(status = SessionStatus.RUNNING)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        useCase = createUseCase()
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS)
        val sessionSlot = slot<Session>()
        coVerify { sessionRepository.saveSession(capture(sessionSlot)) }
        val saved = sessionSlot.captured
        assertEquals(1, saved.totalPowerReadings)
        assertEquals(POWER_WATTS.toLong(), saved.sumPowerWatts)
        assertEquals(POWER_WATTS, saved.maxPowerWatts)
        assertEquals(0.0, saved.totalEnergyJoules!!, 0.001)
    }

    @Test
    fun `second reading calculates energy delta from time difference`() = runTest {
        val sessionBeforeFirst = createSession(
            status = SessionStatus.RUNNING,
        )
        val sessionAfterFirst = createSession(
            status = SessionStatus.RUNNING,
            totalPowerReadings = 1,
            sumPowerWatts = POWER_WATTS.toLong(),
            maxPowerWatts = POWER_WATTS,
            totalEnergyJoules = 0.0,
        )
        coEvery { activeSessionUseCase.getActiveSession() } returnsMany listOf(sessionBeforeFirst, sessionAfterFirst)
        useCase = createUseCase()
        // First call sets internal lastReadingTimestampMs (energy delta = 0 for first)
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS)
        // Second call should calculate energy: 200W * 1s = 200J
        useCase.update(POWER_WATTS, TIMESTAMP_2_MS)
        val savedSessions = mutableListOf<Session>()
        coVerify(exactly = 2) { sessionRepository.saveSession(capture(savedSessions)) }
        val saved = savedSessions[1]
        assertEquals(2, saved.totalPowerReadings)
        assertEquals(POWER_WATTS.toLong() * 2, saved.sumPowerWatts)
        assertEquals(200.0, saved.totalEnergyJoules!!, 0.001)
    }

    @Test
    fun `update tracks max power across readings`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            totalPowerReadings = 1,
            sumPowerWatts = 100L,
            maxPowerWatts = 100,
            totalEnergyJoules = 0.0,
        )
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        useCase = createUseCase()
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS)
        val sessionSlot = slot<Session>()
        coVerify { sessionRepository.saveSession(capture(sessionSlot)) }
        val saved = sessionSlot.captured
        assertEquals(POWER_WATTS, saved.maxPowerWatts)
    }

    @Test
    fun `update does nothing when session is paused`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        useCase = createUseCase()
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS)
        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }
}
