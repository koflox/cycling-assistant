package com.koflox.session.presentation.session.timer

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimerImplTest {

    companion object {
        private const val TIMER_INTERVAL_MS = 1000L
        private const val SESSION_START_TIME_MS = 1000000L
        private const val SESSION_ELAPSED_TIME_MS = 5000L
    }

    @Test
    fun `start triggers onTick after interval for running session`() = runTest {
        val tickValues = mutableListOf<Long>()
        val currentTime = SESSION_START_TIME_MS + SESSION_ELAPSED_TIME_MS + 2000L
        val timer = createTimer(testScope = this, currentTime = currentTime)
        val session = createSession(status = SessionStatus.RUNNING)

        timer.start(session) { tickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS + 1)

        Assert.assertEquals(1, tickValues.size)
        timer.stop()
    }

    @Test
    fun `start calculates elapsed time correctly`() = runTest {
        val tickValues = mutableListOf<Long>()
        val elapsedSinceResume = 3000L
        val currentTime = SESSION_START_TIME_MS + elapsedSinceResume
        val timer = createTimer(testScope = this, currentTime = currentTime)
        val session = createSession(
            status = SessionStatus.RUNNING,
            elapsedTimeMs = SESSION_ELAPSED_TIME_MS,
            lastResumedTimeMs = SESSION_START_TIME_MS,
        )

        timer.start(session) { tickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS + 1)

        val expectedElapsed = SESSION_ELAPSED_TIME_MS + elapsedSinceResume
        Assert.assertEquals(expectedElapsed, tickValues.first())
        timer.stop()
    }

    @Test
    fun `start triggers multiple ticks over time`() = runTest {
        val tickValues = mutableListOf<Long>()
        val timer = createTimer(testScope = this, currentTime = SESSION_START_TIME_MS)
        val session = createSession(status = SessionStatus.RUNNING)

        timer.start(session) { tickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS * 3 + 1)

        Assert.assertEquals(3, tickValues.size)
        timer.stop()
    }

    @Test
    fun `start does not trigger onTick for paused session`() = runTest {
        val tickValues = mutableListOf<Long>()
        val timer = createTimer(testScope = this, currentTime = SESSION_START_TIME_MS)
        val session = createSession(status = SessionStatus.PAUSED)

        timer.start(session) { tickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS * 3 + 1)

        Assert.assertTrue(tickValues.isEmpty())
        timer.stop()
    }

    @Test
    fun `start does not trigger onTick for completed session`() = runTest {
        val tickValues = mutableListOf<Long>()
        val timer = createTimer(testScope = this, currentTime = SESSION_START_TIME_MS)
        val session = createSession(status = SessionStatus.COMPLETED)

        timer.start(session) { tickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS * 3 + 1)

        Assert.assertTrue(tickValues.isEmpty())
        timer.stop()
    }

    @Test
    fun `stop cancels running timer`() = runTest {
        val tickValues = mutableListOf<Long>()
        val timer = createTimer(testScope = this, currentTime = SESSION_START_TIME_MS)
        val session = createSession(status = SessionStatus.RUNNING)

        timer.start(session) { tickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS + 1)
        Assert.assertEquals(1, tickValues.size)

        timer.stop()
        advanceTimeBy(TIMER_INTERVAL_MS * 3)

        Assert.assertEquals(1, tickValues.size)
    }

    @Test
    fun `start cancels previous timer before starting new one`() = runTest {
        val firstTickValues = mutableListOf<Long>()
        val secondTickValues = mutableListOf<Long>()
        val timer = createTimer(testScope = this, currentTime = SESSION_START_TIME_MS)
        val firstSession = createSession(status = SessionStatus.RUNNING, id = "first")
        val secondSession = createSession(status = SessionStatus.RUNNING, id = "second")

        timer.start(firstSession) { firstTickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS + 1)
        Assert.assertEquals(1, firstTickValues.size)

        timer.start(secondSession) { secondTickValues.add(it) }
        advanceTimeBy(TIMER_INTERVAL_MS * 2 + 1)

        Assert.assertEquals(1, firstTickValues.size)
        Assert.assertEquals(2, secondTickValues.size)
        timer.stop()
    }

    @Test
    fun `stop is safe to call when no timer is running`() = runTest {
        val timer = createTimer(testScope = this, currentTime = SESSION_START_TIME_MS)

        timer.stop()
        timer.stop()
    }

    private fun createTimer(
        testScope: TestScope,
        currentTime: Long,
    ): SessionTimerImpl {
        return SessionTimerImpl(
            scope = testScope,
            currentTimeProvider = { currentTime },
        )
    }

    private fun createSession(
        id: String = "session-123",
        status: SessionStatus = SessionStatus.RUNNING,
        elapsedTimeMs: Long = SESSION_ELAPSED_TIME_MS,
        lastResumedTimeMs: Long = SESSION_START_TIME_MS,
    ) = Session(
        id = id,
        destinationId = "dest-456",
        destinationName = "Test Destination",
        destinationLatitude = 52.52,
        destinationLongitude = 13.405,
        startLatitude = 52.50,
        startLongitude = 13.40,
        startTimeMs = SESSION_START_TIME_MS,
        lastResumedTimeMs = lastResumedTimeMs,
        endTimeMs = null,
        elapsedTimeMs = elapsedTimeMs,
        traveledDistanceKm = 0.0,
        averageSpeedKmh = 0.0,
        topSpeedKmh = 0.0,
        status = status,
        trackPoints = emptyList(),
    )
}