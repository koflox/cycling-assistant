package com.koflox.session.data.repository

import com.koflox.session.domain.model.SessionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionFlushDeciderImplTest {

    private lateinit var decider: SessionFlushDeciderImpl

    @Before
    fun setup() {
        decider = SessionFlushDeciderImpl()
    }

    @Test
    fun `shouldFlush returns true for paused status`() {
        assertTrue(decider.shouldFlush(SessionStatus.PAUSED, System.currentTimeMillis()))
    }

    @Test
    fun `shouldFlush returns true for completed status`() {
        assertTrue(decider.shouldFlush(SessionStatus.COMPLETED, System.currentTimeMillis()))
    }

    @Test
    fun `shouldFlush returns true when flush interval exceeded`() {
        val oldTime = System.currentTimeMillis() - 120_000L

        assertTrue(decider.shouldFlush(SessionStatus.RUNNING, oldTime))
    }

    @Test
    fun `shouldFlush returns false for running within interval`() {
        assertFalse(decider.shouldFlush(SessionStatus.RUNNING, System.currentTimeMillis()))
    }

    @Test
    fun `shouldFlush returns true when lastFlushTimeMs is zero`() {
        assertTrue(decider.shouldFlush(SessionStatus.RUNNING, 0L))
    }
}
