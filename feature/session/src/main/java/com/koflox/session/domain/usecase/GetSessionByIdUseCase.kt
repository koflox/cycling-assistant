package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.repository.SessionRepository

interface GetSessionByIdUseCase {
    suspend fun getSession(sessionId: String): Result<Session>
}

internal class GetSessionByIdUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : GetSessionByIdUseCase {
    override suspend fun getSession(sessionId: String): Result<Session> = sessionRepository.getSession(sessionId)
}
