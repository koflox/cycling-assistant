package com.koflox.session.presentation.session.timer

import com.koflox.session.domain.model.Session
import kotlinx.coroutines.CoroutineScope

internal fun interface SessionTimerFactory {
    fun create(scope: CoroutineScope): SessionTimer
}

internal interface SessionTimer {
    fun start(session: Session, onTick: (elapsedMs: Long) -> Unit)
    fun stop()
}
