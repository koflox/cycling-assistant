package com.koflox.session.data.source.runtime

import com.koflox.session.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicReference

internal class SessionRuntimeDataSourceImpl : SessionRuntimeDataSource {

    private val _activeSession = MutableStateFlow<Session?>(null)
    override val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    private val flushInfo = AtomicReference(FlushInfo())

    override fun setActiveSession(session: Session) {
        _activeSession.value = session
    }

    override fun clearActiveSession() {
        _activeSession.value = null
        flushInfo.set(FlushInfo())
    }

    override fun getFlushInfo(): FlushInfo = flushInfo.get()

    override fun setFlushInfo(flushInfo: FlushInfo) {
        this.flushInfo.set(flushInfo)
    }
}
