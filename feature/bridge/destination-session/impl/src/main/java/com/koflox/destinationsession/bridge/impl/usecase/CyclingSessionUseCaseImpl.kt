package com.koflox.destinationsession.bridge.impl.usecase

import com.koflox.concurrent.suspendRunCatching
import com.koflox.destinationsession.bridge.usecase.ActiveSessionDestination
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionParams
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.session.service.SessionServiceController
import kotlinx.coroutines.flow.Flow

internal class CyclingSessionUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val sessionServiceController: SessionServiceController,
) : CyclingSessionUseCase {

    override fun observeHasActiveSession(): Flow<Boolean> = activeSessionUseCase.hasActiveSession()

    override suspend fun getActiveSessionDestination(): ActiveSessionDestination? = try {
        val destinationId = activeSessionUseCase.getActiveSession().destinationId
        destinationId?.let { ActiveSessionDestination(id = it) }
    } catch (_: NoActiveSessionException) {
        null
    }

    override suspend fun startFreeRoamSession(): Result<Unit> = suspendRunCatching {
        createSessionUseCase.create(CreateSessionParams.FreeRoam)
            .onSuccess { sessionServiceController.startSessionTracking() }
            .getOrThrow()
        Unit
    }
}
