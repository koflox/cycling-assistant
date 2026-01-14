package com.koflox.session.domain.usecase

import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.StateFlow

interface ActiveSessionUseCase {

    val hasActiveSession: StateFlow<Boolean>

    fun setActive(isActive: Boolean)
}

internal class ActiveSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : ActiveSessionUseCase {

    override val hasActiveSession: StateFlow<Boolean>
        get() = sessionRepository.hasActiveSession

    override fun setActive(isActive: Boolean) {
        sessionRepository.setActiveSession(isActive)
    }
}
