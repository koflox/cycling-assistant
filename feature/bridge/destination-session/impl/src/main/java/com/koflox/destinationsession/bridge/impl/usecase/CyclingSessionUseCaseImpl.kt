package com.koflox.destinationsession.bridge.impl.usecase

import com.koflox.destinationsession.bridge.usecase.ActiveSessionDestination
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.NoActiveSessionException
import kotlinx.coroutines.flow.Flow

internal class CyclingSessionUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
) : CyclingSessionUseCase {

    override fun observeHasActiveSession(): Flow<Boolean> = activeSessionUseCase.hasActiveSession()

    override suspend fun getActiveSessionDestination(): ActiveSessionDestination? = try {
        val destinationId = activeSessionUseCase.getActiveSession().destinationId
        destinationId?.let { ActiveSessionDestination(id = it) }
    } catch (_: NoActiveSessionException) {
        null
    }
}
