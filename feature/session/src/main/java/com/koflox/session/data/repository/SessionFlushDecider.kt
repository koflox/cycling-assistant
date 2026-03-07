package com.koflox.session.data.repository

import com.koflox.session.domain.model.SessionStatus
import kotlin.time.Duration.Companion.minutes

internal interface SessionFlushDecider {
    fun shouldFlush(status: SessionStatus, lastFlushTimeMs: Long): Boolean
}

internal class SessionFlushDeciderImpl : SessionFlushDecider {

    companion object {
        private val FLUSH_INTERVAL = 1.minutes
    }

    override fun shouldFlush(status: SessionStatus, lastFlushTimeMs: Long): Boolean = status == SessionStatus.PAUSED
            || status == SessionStatus.COMPLETED
            || System.currentTimeMillis() - lastFlushTimeMs >= FLUSH_INTERVAL.inWholeMilliseconds
}
