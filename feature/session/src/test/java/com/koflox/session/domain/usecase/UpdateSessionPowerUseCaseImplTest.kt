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
        private const val MIN_WINDOW_READINGS = 3
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
        sessionMutex = mutex,
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
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS)
        useCase.update(POWER_WATTS, TIMESTAMP_2_MS)
        val savedSessions = mutableListOf<Session>()
        coVerify(exactly = 2) { sessionRepository.saveSession(capture(savedSessions)) }
        val saved = savedSessions[1]
        assertEquals(2, saved.totalPowerReadings)
        assertEquals(POWER_WATTS.toLong() * 2, saved.sumPowerWatts)
        assertEquals(200.0, saved.totalEnergyJoules!!, 0.001)
    }

    @Test
    fun `max power not updated during warmup period`() = runTest {
        val session = createSession(
            status = SessionStatus.RUNNING,
            maxPowerWatts = 0,
        )
        coEvery { activeSessionUseCase.getActiveSession() } returns session
        useCase = createUseCase()
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS)
        useCase.update(POWER_WATTS, TIMESTAMP_1_MS + 1000L)
        val savedSessions = mutableListOf<Session>()
        coVerify(exactly = 2) { sessionRepository.saveSession(capture(savedSessions)) }
        assertEquals(0, savedSessions[0].maxPowerWatts)
        assertEquals(0, savedSessions[1].maxPowerWatts)
    }

    @Test
    fun `max power updated after minimum window readings reached`() = runTest {
        useCase = createUseCase()
        val savedSessions = mutableListOf<Session>()
        for (i in 1..MIN_WINDOW_READINGS) {
            val session = createSession(
                status = SessionStatus.RUNNING,
                totalPowerReadings = i - 1,
                sumPowerWatts = (i - 1) * POWER_WATTS.toLong(),
                maxPowerWatts = 0,
            )
            coEvery { activeSessionUseCase.getActiveSession() } returns session
            useCase.update(POWER_WATTS, TIMESTAMP_1_MS + i * 1000L)
        }
        coVerify(exactly = MIN_WINDOW_READINGS) { sessionRepository.saveSession(capture(savedSessions)) }
        assertEquals(POWER_WATTS, savedSessions.last().maxPowerWatts)
    }

    @Test
    fun `median filter smooths out power spike for max power`() = runTest {
        useCase = createUseCase()
        val normalPower = 100
        val spikePower = 700
        val powers = listOf(normalPower, normalPower, spikePower, normalPower, normalPower)
        val savedSessions = mutableListOf<Session>()
        for (i in powers.indices) {
            val session = createSession(
                status = SessionStatus.RUNNING,
                totalPowerReadings = i,
                sumPowerWatts = powers.take(i).sumOf { it.toLong() },
                maxPowerWatts = 0,
            )
            coEvery { activeSessionUseCase.getActiveSession() } returns session
            useCase.update(powers[i], TIMESTAMP_1_MS + i * 1000L)
        }
        coVerify(exactly = powers.size) { sessionRepository.saveSession(capture(savedSessions)) }
        val lastSaved = savedSessions.last()
        assertEquals(normalPower, lastSaved.maxPowerWatts)
    }

    @Test
    fun `old readings are evicted from window`() = runTest {
        useCase = createUseCase()
        val savedSessions = mutableListOf<Session>()
        // 3 readings at t=0s, t=1s, t=2s with power=300
        for (i in 0..2) {
            val session = createSession(
                status = SessionStatus.RUNNING,
                totalPowerReadings = i,
                sumPowerWatts = i * 300L,
                maxPowerWatts = 300,
            )
            coEvery { activeSessionUseCase.getActiveSession() } returns session
            useCase.update(300, TIMESTAMP_1_MS + i * 1000L)
        }
        // 3 readings at t=11s, t=12s, t=13s with power=100 (old readings outside 10s window)
        for (i in 0..2) {
            val readingIndex = 3 + i
            val session = createSession(
                status = SessionStatus.RUNNING,
                totalPowerReadings = readingIndex,
                sumPowerWatts = 3 * 300L + i * 100L,
                maxPowerWatts = 300,
            )
            coEvery { activeSessionUseCase.getActiveSession() } returns session
            useCase.update(100, TIMESTAMP_1_MS + 11_000L + i * 1000L)
        }
        coVerify(exactly = 6) { sessionRepository.saveSession(capture(savedSessions)) }
        val lastSaved = savedSessions.last()
        assertEquals(300, lastSaved.maxPowerWatts)
    }

    @Test
    fun `sum and readings use raw values not filtered`() = runTest {
        useCase = createUseCase()
        val spikePower = 700
        val normalPower = 100
        val powers = listOf(normalPower, normalPower, spikePower, normalPower, normalPower)
        val savedSessions = mutableListOf<Session>()
        for (i in powers.indices) {
            val session = createSession(
                status = SessionStatus.RUNNING,
                totalPowerReadings = i,
                sumPowerWatts = powers.take(i).sumOf { it.toLong() },
                maxPowerWatts = 0,
            )
            coEvery { activeSessionUseCase.getActiveSession() } returns session
            useCase.update(powers[i], TIMESTAMP_1_MS + i * 1000L)
        }
        coVerify(exactly = powers.size) { sessionRepository.saveSession(capture(savedSessions)) }
        val lastSaved = savedSessions.last()
        assertEquals(powers.size, lastSaved.totalPowerReadings)
        assertEquals(powers.sumOf { it.toLong() }, lastSaved.sumPowerWatts)
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
