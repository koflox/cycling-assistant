package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

interface GetAllSessionsUseCase {
    fun observeAllSessions(): Flow<List<Session>>
}

class GetAllSessionsUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : GetAllSessionsUseCase {
    override fun observeAllSessions(): Flow<List<Session>> = sessionRepository.observeAllSessions()
}
