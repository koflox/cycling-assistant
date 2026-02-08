package com.koflox.session.service

import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.model.Location
import com.koflox.location.settings.LocationSettingsDataSource
import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.testutil.createSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
        private const val TEST_LATITUDE = 50.0
        private const val TEST_LONGITUDE = 14.0
    }

    private val testDispatcher = StandardTestDispatcher()
    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val updateSessionLocationUseCase: UpdateSessionLocationUseCase = mockk()
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase = mockk()
    private val locationDataSource: LocationDataSource = mockk()
    private val locationSettingsDataSource: LocationSettingsDataSource = mockk()
    private val nutritionReminderUseCase: NutritionReminderUseCase = mockk()
    private val delegate: SessionTrackingDelegate = mockk(relaxed = true)
    private var currentTimeMs = LAST_RESUMED_TIME_MS

    private val sessionFlow = MutableStateFlow<Session?>(null)
    private val locationEnabledFlow = MutableStateFlow(true)
    private val nutritionFlow = MutableSharedFlow<Unit>()

    private lateinit var tracker: SessionTrackerImpl

    @Before
    fun setup() {
        every { activeSessionUseCase.observeActiveSession() } returns sessionFlow
        every { locationSettingsDataSource.observeLocationEnabled() } returns locationEnabledFlow
        every { nutritionReminderUseCase.observeNutritionReminders() } returns nutritionFlow
        coEvery { updateSessionLocationUseCase.update(any(), any()) } returns Unit
        coEvery { updateSessionStatusUseCase.pause() } returns Result.success(Unit)
        coEvery { updateSessionStatusUseCase.resume() } returns Result.success(Unit)
        coEvery { updateSessionStatusUseCase.stop() } returns Result.success(Unit)
        coEvery { locationDataSource.getCurrentLocation() } returns Result.success(
            Location(latitude = TEST_LATITUDE, longitude = TEST_LONGITUDE),
        )
        tracker = createTracker()
    }

    @Test
    fun `startTracking observes active session`() = runTrackerTest {
        tracker.startTracking(delegate)
        advanceUntilIdle()

        verify { activeSessionUseCase.observeActiveSession() }
    }

    @Test
    fun `null session calls delegate onStopService`() = runTrackerTest {
        sessionFlow.value = null
        tracker.startTracking(delegate)
        advanceUntilIdle()

        verify { delegate.onStopService() }
    }

    @Test
    fun `running session starts location collection`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS + 1)

        coVerify { locationDataSource.getCurrentLocation() }
    }

    @Test
    fun `running session starts timer that updates notification`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.TIMER_UPDATE_INTERVAL_MS + 1)

        verify { delegate.onNotificationUpdate(session, any()) }
    }

    @Test
    fun `running session starts location monitoring`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        verify { locationSettingsDataSource.observeLocationEnabled() }
    }

    @Test
    fun `paused session stops location collection and timer`() = runTrackerTest {
        val runningSession = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = runningSession
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS + 1)

        val pausedSession = createTestSession(status = SessionStatus.PAUSED)
        sessionFlow.value = pausedSession
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS * 2)

        verify { delegate.onNotificationUpdate(pausedSession, ELAPSED_TIME_MS) }
    }

    @Test
    fun `completed session calls delegate onStopService`() = runTrackerTest {
        val runningSession = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = runningSession
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        val completedSession = createTestSession(status = SessionStatus.COMPLETED)
        sessionFlow.value = completedSession
        advanceUntilIdle()

        verify { delegate.onStopService() }
    }

    @Test
    fun `location collection polls every 3 seconds`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS * 3 + 1)

        coVerify(atLeast = 3) { locationDataSource.getCurrentLocation() }
    }

    @Test
    fun `location update calls updateSessionLocationUseCase`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS + 1)

        coVerify { updateSessionLocationUseCase.update(any(), any()) }
    }

    @Test
    fun `timer calls delegate onNotificationUpdate`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.TIMER_UPDATE_INTERVAL_MS * 3 + 1)

        verify(atLeast = 3) { delegate.onNotificationUpdate(session, any()) }
    }

    @Test
    fun `location disabled triggers session pause`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        locationEnabledFlow.value = false
        advanceTimeBy(1)

        coVerify { updateSessionStatusUseCase.pause() }
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
    fun `stopTracking cancels all jobs`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS + 1)

        tracker.stopTracking()
        advanceTimeBy(SessionTrackerImpl.LOCATION_INTERVAL_MS * 3)

        coVerify(atMost = 1) { locationDataSource.getCurrentLocation() }
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
    fun `handleRestart stops service when no active session`() = runTrackerTest {
        sessionFlow.value = null
        every { activeSessionUseCase.observeActiveSession() } returns flowOf(null)
        tracker.handleRestart(delegate)
        advanceUntilIdle()

        verify { delegate.onStopService() }
    }

    @Test
    fun `nutrition reminder triggers delegate onVibrate`() = runTrackerTest {
        val session = createTestSession(status = SessionStatus.RUNNING)
        sessionFlow.value = session
        tracker.startTracking(delegate)
        advanceTimeBy(1)

        nutritionFlow.emit(Unit)
        advanceTimeBy(1)

        verify { delegate.onVibrate() }
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
        updateSessionLocationUseCase = updateSessionLocationUseCase,
        updateSessionStatusUseCase = updateSessionStatusUseCase,
        locationDataSource = locationDataSource,
        locationSettingsDataSource = locationSettingsDataSource,
        nutritionReminderUseCase = nutritionReminderUseCase,
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
