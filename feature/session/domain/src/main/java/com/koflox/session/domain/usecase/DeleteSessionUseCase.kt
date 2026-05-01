package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository

interface DeleteSessionUseCase {
    suspend fun delete(sessionId: String): Result<Unit>
}

class DeleteSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : DeleteSessionUseCase {

    override suspend fun delete(sessionId: String): Result<Unit> =
        sessionRepository.getSession(sessionId).fold(
            onSuccess = { session ->
                if (session.status != SessionStatus.COMPLETED) {
                    Result.failure(SessionNotDeletableException(session.status))
                } else {
                    sessionRepository.deleteSession(sessionId)
                }
            },
            onFailure = { Result.failure(it) },
        )
}

class SessionNotDeletableException(val status: SessionStatus) :
    IllegalStateException("Cannot delete session with status $status — only COMPLETED sessions are deletable")
