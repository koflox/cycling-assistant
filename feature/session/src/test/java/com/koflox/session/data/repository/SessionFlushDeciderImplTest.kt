package com.koflox.session.data.repository

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.session.domain.model.SessionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionFlushDeciderImplTest {

    private var currentTimeMs = 1000000L

    private val currentTimeProvider = CurrentTimeProvider { currentTimeMs }
    private val decider = SessionFlushDeciderImpl(currentTimeProvider)

    @Test
    fun `shouldFlush returns true for paused status`() {
        assertTrue(decider.shouldFlush(SessionStatus.PAUSED, currentTimeMs))
    }

    @Test
    fun `shouldFlush returns true for completed status`() {
        assertTrue(decider.shouldFlush(SessionStatus.COMPLETED, currentTimeMs))
    }

    @Test
    fun `shouldFlush returns true when flush interval exceeded`() {
        val oldTime = currentTimeMs - 120_000L

        assertTrue(decider.shouldFlush(SessionStatus.RUNNING, oldTime))
    }

    @Test
    fun `shouldFlush returns false for running within interval`() {
        assertFalse(decider.shouldFlush(SessionStatus.RUNNING, currentTimeMs))
    }

    @Test
    fun `shouldFlush returns true when lastFlushTimeMs is zero`() {
        assertTrue(decider.shouldFlush(SessionStatus.RUNNING, 0L))
    }
}
