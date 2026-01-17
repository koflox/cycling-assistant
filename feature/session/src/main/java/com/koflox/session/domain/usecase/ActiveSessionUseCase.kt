package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface ActiveSessionUseCase {
    fun observeActiveSession(): Flow<Session?>
    fun hasActiveSession(): Flow<Boolean>
    suspend fun getActiveSession(): Session
}

class NoActiveSessionException : IllegalStateException("No active session")

internal class ActiveSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : ActiveSessionUseCase {

    override fun observeActiveSession(): Flow<Session?> = sessionRepository.observeActiveSession()

    override fun hasActiveSession(): Flow<Boolean> = sessionRepository.observeActiveSession().map { it != null }

    override suspend fun getActiveSession(): Session = sessionRepository.observeActiveSession().first()
        ?: throw NoActiveSessionException()

}
