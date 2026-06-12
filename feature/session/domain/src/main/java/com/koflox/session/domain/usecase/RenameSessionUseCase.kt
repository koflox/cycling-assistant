package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository

interface RenameSessionUseCase {
    suspend fun rename(sessionId: String, name: String): Result<Unit>
}

class RenameSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : RenameSessionUseCase {

    override suspend fun rename(sessionId: String, name: String): Result<Unit> =
        sessionRepository.getSession(sessionId).fold(
            onSuccess = { session ->
                if (session.status != SessionStatus.COMPLETED) {
                    Result.failure(SessionNotRenamableException(session.status))
                } else {
                    sessionRepository.renameSession(sessionId, name.trim())
                }
            },
            onFailure = { Result.failure(it) },
        )
}

class SessionNotRenamableException(val status: SessionStatus) :
    IllegalStateException("Cannot rename session with status $status — only COMPLETED sessions are renamable")
