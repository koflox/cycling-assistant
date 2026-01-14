package com.koflox.session.domain.repository

import com.koflox.session.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    val hasActiveSession: StateFlow<Boolean>

    fun setActiveSession(isActive: Boolean)

    suspend fun saveSession(session: Session): Result<Unit>

    suspend fun getSession(sessionId: String): Result<Session?>

    fun observeCompletedSessions(): Flow<List<Session>>
}
