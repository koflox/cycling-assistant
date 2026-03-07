package com.koflox.session.data.repository

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.session.domain.model.SessionStatus
import kotlin.time.Duration.Companion.minutes

internal interface SessionFlushDecider {
    fun shouldFlush(status: SessionStatus, lastFlushTimeMs: Long): Boolean
}

internal class SessionFlushDeciderImpl(
    private val currentTimeProvider: CurrentTimeProvider,
) : SessionFlushDecider {

    companion object {
        private val FLUSH_INTERVAL = 1.minutes
    }

    override fun shouldFlush(status: SessionStatus, lastFlushTimeMs: Long): Boolean = status == SessionStatus.PAUSED
        || status == SessionStatus.COMPLETED
        || currentTimeProvider.currentTimeMs() - lastFlushTimeMs >= FLUSH_INTERVAL.inWholeMilliseconds
}
