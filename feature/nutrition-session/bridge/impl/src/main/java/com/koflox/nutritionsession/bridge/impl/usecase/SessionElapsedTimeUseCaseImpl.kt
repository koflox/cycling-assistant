package com.koflox.nutritionsession.bridge.impl.usecase

import com.koflox.nutritionsession.bridge.model.SessionTimeInfo
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SessionElapsedTimeUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
) : SessionElapsedTimeUseCase {

    override fun observeSessionTimeInfo(): Flow<SessionTimeInfo?> =
        activeSessionUseCase.observeActiveSession().map { session ->
            session?.let {
                SessionTimeInfo(
                    elapsedTimeMs = it.elapsedTimeMs,
                    lastResumedTimeMs = it.lastResumedTimeMs,
                    isRunning = it.status == SessionStatus.RUNNING,
                )
            }
        }
}
