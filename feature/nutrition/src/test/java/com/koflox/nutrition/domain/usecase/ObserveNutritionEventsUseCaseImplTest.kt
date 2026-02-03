package com.koflox.nutrition.domain.usecase

import app.cash.turbine.test
import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutritionsession.bridge.model.SessionTimeInfo
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicLong

class ObserveNutritionEventsUseCaseImplTest {

    companion object {
        private const val TEST_INTERVAL_MS = 5000L
        private const val BASE_TIME = 1000000L
    }

    private val sessionElapsedTimeUseCase: SessionElapsedTimeUseCase = mockk()
    private val sessionTimeInfoFlow = MutableSharedFlow<SessionTimeInfo?>()
    private val currentTime = AtomicLong(BASE_TIME)

    private lateinit var useCase: ObserveNutritionEventsUseCaseImpl

    @Before
    fun setup() {
        currentTime.set(BASE_TIME)
        every { sessionElapsedTimeUseCase.observeSessionTimeInfo() } returns sessionTimeInfoFlow
        useCase = ObserveNutritionEventsUseCaseImpl(
            sessionElapsedTimeUseCase = sessionElapsedTimeUseCase,
            currentTimeProvider = { currentTime.get() },
            intervalMs = TEST_INTERVAL_MS,
            checkIntervalMs = Long.MAX_VALUE, // Disable periodic checks for unit tests
        )
    }

    @Test
    fun `emits BreakRequired when first interval is reached`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo)

            val event = awaitItem()
            assertTrue(event is NutritionEvent.BreakRequired)
            assertEquals(1, (event as NutritionEvent.BreakRequired).intervalNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits BreakRequired for multiple intervals`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS * 3,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo)

            val event = awaitItem()
            assertTrue(event is NutritionEvent.BreakRequired)
            assertEquals(3, (event as NutritionEvent.BreakRequired).intervalNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit when elapsed time is below interval`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS - 1000,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit when session is paused`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS * 2,
                lastResumedTimeMs = currentTime.get(),
                isRunning = false,
            )

            sessionTimeInfoFlow.emit(timeInfo)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits ChecksStopped when session becomes null after having session`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo)
            awaitItem() // BreakRequired

            sessionTimeInfoFlow.emit(null)

            val event = awaitItem()
            assertEquals(NutritionEvent.ChecksStopped, event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit ChecksStopped when session is null without prior session`() = runTest {
        useCase.observeNutritionEvents().test {
            sessionTimeInfoFlow.emit(null)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit duplicate interval`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo1 = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )
            val timeInfo2 = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS + 500,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo1)
            awaitItem() // First BreakRequired

            sessionTimeInfoFlow.emit(timeInfo2)
            expectNoEvents() // Should not emit again for same interval

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits next interval after previous one`() = runTest {
        useCase.observeNutritionEvents().test {
            val timeInfo1 = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )
            val timeInfo2 = SessionTimeInfo(
                elapsedTimeMs = TEST_INTERVAL_MS * 2,
                lastResumedTimeMs = currentTime.get(),
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo1)
            val event1 = awaitItem()
            assertEquals(1, (event1 as NutritionEvent.BreakRequired).intervalNumber)

            sessionTimeInfoFlow.emit(timeInfo2)
            val event2 = awaitItem()
            assertEquals(2, (event2 as NutritionEvent.BreakRequired).intervalNumber)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resets interval counter after session stops`() = runTest {
        useCase.observeNutritionEvents().test {
            // First session
            sessionTimeInfoFlow.emit(
                SessionTimeInfo(
                    elapsedTimeMs = TEST_INTERVAL_MS * 2,
                    lastResumedTimeMs = currentTime.get(),
                    isRunning = true,
                ),
            )
            val event1 = awaitItem()
            assertEquals(2, (event1 as NutritionEvent.BreakRequired).intervalNumber)

            // Stop session
            sessionTimeInfoFlow.emit(null)
            awaitItem() // ChecksStopped

            // New session starts at interval 1
            sessionTimeInfoFlow.emit(
                SessionTimeInfo(
                    elapsedTimeMs = TEST_INTERVAL_MS,
                    lastResumedTimeMs = currentTime.get(),
                    isRunning = true,
                ),
            )
            val event2 = awaitItem()
            assertEquals(1, (event2 as NutritionEvent.BreakRequired).intervalNumber)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calculates interval using real elapsed time`() = runTest {
        useCase.observeNutritionEvents().test {
            val startTime = currentTime.get()
            // Session started with 0 elapsed, but time has passed since lastResumed
            currentTime.set(startTime + TEST_INTERVAL_MS)

            val timeInfo = SessionTimeInfo(
                elapsedTimeMs = 0,
                lastResumedTimeMs = startTime,
                isRunning = true,
            )

            sessionTimeInfoFlow.emit(timeInfo)

            val event = awaitItem()
            assertTrue(event is NutritionEvent.BreakRequired)
            assertEquals(1, (event as NutritionEvent.BreakRequired).intervalNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits ChecksStopped after paused session becomes null`() = runTest {
        useCase.observeNutritionEvents().test {
            // Session starts paused
            sessionTimeInfoFlow.emit(
                SessionTimeInfo(
                    elapsedTimeMs = TEST_INTERVAL_MS,
                    lastResumedTimeMs = currentTime.get(),
                    isRunning = false,
                ),
            )
            // No event for paused session

            // Session ends
            sessionTimeInfoFlow.emit(null)

            val event = awaitItem()
            assertEquals(NutritionEvent.ChecksStopped, event)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
