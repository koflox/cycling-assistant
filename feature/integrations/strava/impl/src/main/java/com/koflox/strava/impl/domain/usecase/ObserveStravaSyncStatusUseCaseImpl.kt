package com.koflox.strava.impl.domain.usecase

import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.usecase.ObserveStravaSyncStatusUseCase
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ObserveStravaSyncStatusUseCaseImpl @Inject constructor(
    private val repository: StravaSyncRepository,
) : ObserveStravaSyncStatusUseCase {

    override fun observe(sessionId: String): Flow<SessionSyncStatus> = repository.observe(sessionId)

    override fun observeAll(): Flow<Map<String, SessionSyncStatus>> = repository.observeAll()
}
