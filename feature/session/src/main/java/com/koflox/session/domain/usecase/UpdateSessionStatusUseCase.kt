package com.koflox.session.domain.usecase

import com.koflox.concurrent.suspendRunCatching
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository

interface UpdateSessionStatusUseCase {
    suspend fun pause(): Result<Unit>
    suspend fun resume(): Result<Unit>
    suspend fun stop(): Result<Unit>
}

internal class UpdateSessionStatusUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionRepository: SessionRepository,
) : UpdateSessionStatusUseCase {

    override suspend fun pause(): Result<Unit> = suspendRunCatching {
        val session = activeSessionUseCase.getActiveSession()
        val currentTimeMs = System.currentTimeMillis()
        val elapsedSinceLastResume = currentTimeMs - session.lastResumedTimeMs
        val totalElapsedTimeMs = session.elapsedTimeMs + elapsedSinceLastResume
        val pausedSession = session.copy(
            status = SessionStatus.PAUSED,
            elapsedTimeMs = totalElapsedTimeMs,
        )
        sessionRepository.saveSession(pausedSession).getOrThrow()
    }

    override suspend fun resume(): Result<Unit> = suspendRunCatching {
        val session = activeSessionUseCase.getActiveSession()
        val currentTimeMs = System.currentTimeMillis()
        val resumedSession = session.copy(
            status = SessionStatus.RUNNING,
            lastResumedTimeMs = currentTimeMs,
        )
        sessionRepository.saveSession(resumedSession).getOrThrow()
    }

    override suspend fun stop(): Result<Unit> = suspendRunCatching {
        val session = activeSessionUseCase.getActiveSession()
        val currentTimeMs = System.currentTimeMillis()
        val finalElapsedTimeMs = if (session.status == SessionStatus.RUNNING) {
            session.elapsedTimeMs + (currentTimeMs - session.lastResumedTimeMs)
        } else {
            session.elapsedTimeMs
        }
        val completedSession = session.copy(
            status = SessionStatus.COMPLETED,
            endTimeMs = currentTimeMs,
            elapsedTimeMs = finalElapsedTimeMs,
        )
        sessionRepository.saveSession(completedSession).getOrThrow()
    }
}
