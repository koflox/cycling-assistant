package com.koflox.session.service

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.testutil.createSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTrackerImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val LAST_RESUMED_TIME_MS = 1000000L
        private const val ELAPSED_TIME_MS = 60000L
    }

    private val testDispatcher = StandardTestDispatcher()
    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase = mockk()
    private val locationCollectionManager: LocationCollectionManager = mockk(relaxed = true)
    private val powerCollectionManager: PowerCollectionManager = mockk(relaxed = true)
    private val nutritionReminderManager: NutritionReminderManager = mockk(relaxed = true)
    private val delegate: SessionTrackingDelegate = mockk(relaxed = true)
    private var currentTimeMs = LAST_RESUMED_TIME_MS

    private val sessionFlow = MutableStateFlow<Session?>(null)

    private lateinit var tracker: SessionTrackerImpl

    @Before
    fun setup() {
        every { activeSessionUseCase.observeActiveSession() } returns sessionFlow
        coEvery { updateSessionStatusUseCase.pause() } returns Result.success(Unit)
        coEvery { updateSessionStatusUseCase.resume() } returns Result.success(Unit)
        coEvery { updateSessionStatusUseCase.stop() } returns Result.success(Unit)
        coEvery { updateSessionStatusUseCase.onServiceRestart() } returns Result.success(Unit)
        tracker = createTracker()
    }

    @Test
    fun `startTracking observes active session`() = runTrackerTest {
        tracker.startTracking(delegate)
        advanceUntilIdle()

        verify { activeSessionUseCase.observeActiveSession() }
    }

    @Test
    fun `startTracking starts nutrition reminder manager`() = runTrackerTest {
        tracker.startTracking(delegate)
        advanceUntilIdle()

        verify { nutritionReminderManager.start(any(), any()) }
    }

    @Test
    fun `null session calls delegate onStopService`() = runTrackerTest {
        sessionFlow.value = null
        tracker.startTracking(delegate)
        advanceUntilIdle()

        verify { delegate.onStopService() }
    }

    @Test
    fun `null session stops managers`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        sessionFlow.value = null
        advanceTimeBy(1)

        verify { locationCollectionManager.stop() }
        verify { powerCollectionManager.stop() }
    }

    @Test
    fun `running session starts location collection manager`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        verify { locationCollectionManager.start(any()) }
    }

    @Test
    fun `running session starts power collection manager`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        verify { powerCollectionManager.start(any()) }
    }

    @Test
    fun `running session starts timer that updates notification`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.TIMER_UPDATE_INTERVAL.inWholeMilliseconds + 1)

        verify { delegate.onNotificationUpdate(session, any()) }
    }

    @Test
    fun `paused session stops managers and updates notification`() = runTrackerTest {
        val runningSession = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = runningSession
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        val pausedSession = createTestSession(status = SessionStatus.PAUSED)
        sessionFlow.value = pausedSession
        advanceTimeBy(1)

        verify { locationCollectionManager.stop() }
        verify { powerCollectionManager.stop() }
        verify { delegate.onNotificationUpdate(pausedSession, ELAPSED_TIME_MS) }
    }

    @Test
    fun `timer calls delegate onNotificationUpdate`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.TIMER_UPDATE_INTERVAL.inWholeMilliseconds * 3 + 1)

        verify(atLeast = 3) { delegate.onNotificationUpdate(session, any()) }
    }

    @Test
    fun `pauseSession delegates to updateSessionStatusUseCase`() = runTrackerTest {
        tracker.startTracking(delegate)
        advanceUntilIdle()

        tracker.pauseSession()
        advanceUntilIdle()

        coVerify { updateSessionStatusUseCase.pause() }
    }

    @Test
    fun `resumeSession delegates to updateSessionStatusUseCase`() = runTrackerTest {
        tracker.startTracking(delegate)
        advanceUntilIdle()

        tracker.resumeSession()
        advanceUntilIdle()

        coVerify { updateSessionStatusUseCase.resume() }
    }

    @Test
    fun `stopSession delegates to updateSessionStatusUseCase`() = runTrackerTest {
        tracker.startTracking(delegate)
        advanceUntilIdle()

        tracker.stopSession()
        advanceUntilIdle()

        coVerify { updateSessionStatusUseCase.stop() }
    }

    @Test
    fun `stopTracking stops all managers`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        tracker.stopTracking()

        verify { locationCollectionManager.stop() }
        verify { powerCollectionManager.stop() }
        verify { nutritionReminderManager.stop() }
    }

    @Test
    fun `handleRestart starts tracking when active session exists`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.handleRestart(delegate)
        advanceTimeBy(1)

        verify { delegate.onStartForeground() }
        verify { activeSessionUseCase.observeActiveSession() }
    }

    @Test
    fun `handleRestart calls onServiceRestart before observing session`() = runTrackerTest {
        every { delegate.onStartForeground() } returns true
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.handleRestart(delegate)
        advanceTimeBy(1)

        coVerify { updateSessionStatusUseCase.onServiceRestart() }
    }

    @Test
    fun `handleRestart starts nutrition reminder manager`() = runTrackerTest {
        every { delegate.onStartForeground() } returns true
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.handleRestart(delegate)
        advanceTimeBy(1)

        verify { nutritionReminderManager.start(any(), any()) }
    }

    @Test
    fun `handleRestart stops service when no active session`() = runTrackerTest {
        sessionFlow.value = null
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(null)
        tracker.handleRestart(delegate)
        advanceUntilIdle()

        verify { delegate.onStopService() }
    }

    @Test
    fun `session becoming null stops timer and notification updates`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.TIMER_UPDATE_INTERVAL.inWholeMilliseconds + 1)

        verify(atLeast = 1) { delegate.onNotificationUpdate(session, any()) }

        sessionFlow.value = null
        advanceTimeBy(1)

        verify { delegate.onStopService() }

        // Reset notification mock and advance time — no more updates should happen
        io.mockk.clearMocks(delegate, answers = false, recordedCalls = true, verificationMarks = true)
        advanceTimeBy(SessionTrackerImpl.TIMER_UPDATE_INTERVAL.inWholeMilliseconds * 3)

        verify(exactly = 0) { delegate.onNotificationUpdate(any(), any()) }
    }

    private fun runTrackerTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            block()
        } finally {
            tracker.stopTracking()
        }
    }

    private fun createTracker() = SessionTrackerImpl(
        dispatcherIo = testDispatcher,
        activeSessionUseCase = activeSessionUseCase,
        updateSessionStatusUseCase = updateSessionStatusUseCase,
        locationCollectionManager = locationCollectionManager,
        powerCollectionManager = powerCollectionManager,
        nutritionReminderManager = nutritionReminderManager,
        currentTimeProvider = { currentTimeMs },
    )

    private fun createTestSession(
        id: String = SESSION_ID,
        status: SessionStatus = SessionStatus.RUNNING,
        elapsedTimeMs: Long = ELAPSED_TIME_MS,
        lastResumedTimeMs: Long = LAST_RESUMED_TIME_MS,
    ) = createSession(
        id = id,
        status = status,
        elapsedTimeMs = elapsedTimeMs,
        lastResumedTimeMs = lastResumedTimeMs,
    )
}
