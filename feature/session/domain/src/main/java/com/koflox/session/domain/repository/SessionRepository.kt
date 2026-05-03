package com.koflox.session.domain.repository

import com.koflox.session.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    fun observeActiveSession(): Flow<Session?>

    fun observeAllSessions(): Flow<List<Session>>

    suspend fun saveSession(session: Session): Result<Unit>

    suspend fun getSession(sessionId: String): Result<Session>

    suspend fun deleteSession(sessionId: String): Result<Unit>

    suspend fun renameSession(sessionId: String, name: String): Result<Unit>

}
