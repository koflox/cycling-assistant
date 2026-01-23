package com.koflox.session.presentation.session.timer

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SessionTimerImpl(
    private val scope: CoroutineScope,
    private val currentTimeProvider: () -> Long = { System.currentTimeMillis() },
) : SessionTimer {

    companion object {
        private const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }

    private var timerJob: Job? = null

    override fun start(session: Session, onTick: (elapsedMs: Long) -> Unit) {
        stop()
        timerJob = scope.launch {
            while (isActive) {
                delay(TIMER_UPDATE_INTERVAL_MS)
                if (session.status == SessionStatus.RUNNING) {
                    val elapsedSinceLastResume = currentTimeProvider() - session.lastResumedTimeMs
                    val totalElapsedMs = session.elapsedTimeMs + elapsedSinceLastResume
                    onTick(totalElapsedMs)
                }
            }
        }
    }

    override fun stop() {
        timerJob?.cancel()
        timerJob = null
    }
}
