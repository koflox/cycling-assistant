package com.koflox.session.service

import com.koflox.session.service.PendingSessionActionImpl.Companion.ACTION_STOP_CONFIRMATION
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PendingSessionActionImplTest {

    private lateinit var pendingSessionAction: PendingSessionActionImpl

    @Before
    fun setup() {
        pendingSessionAction = PendingSessionActionImpl()
    }

    @Test
    fun `initial isStopRequested is false`() {
        assertFalse(pendingSessionAction.isStopRequested.value)
    }

    @Test
    fun `handleIntentAction with ACTION_STOP_CONFIRMATION sets isStopRequested to true`() {
        pendingSessionAction.handleIntentAction(ACTION_STOP_CONFIRMATION)

        assertTrue(pendingSessionAction.isStopRequested.value)
    }

    @Test
    fun `handleIntentAction with unknown action does not change isStopRequested`() {
        pendingSessionAction.handleIntentAction("com.koflox.session.UNKNOWN_ACTION")

        assertFalse(pendingSessionAction.isStopRequested.value)
    }

    @Test
    fun `consumeStopRequest resets isStopRequested to false`() {
        pendingSessionAction.handleIntentAction(ACTION_STOP_CONFIRMATION)
        assertTrue(pendingSessionAction.isStopRequested.value)

        pendingSessionAction.consumeStopRequest()

        assertFalse(pendingSessionAction.isStopRequested.value)
    }
}
