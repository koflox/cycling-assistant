package com.koflox.session.data.source.runtime

import com.koflox.session.domain.model.Session
import kotlinx.coroutines.flow.StateFlow

internal interface SessionRuntimeDataSource {
    val activeSession: StateFlow<Session?>
    fun setActiveSession(session: Session)
    fun clearActiveSession()
    fun getFlushInfo(): FlushInfo
    fun setFlushInfo(flushInfo: FlushInfo)
}
