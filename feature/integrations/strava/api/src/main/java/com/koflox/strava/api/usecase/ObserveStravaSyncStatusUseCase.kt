package com.koflox.strava.api.usecase

import com.koflox.strava.api.model.SessionSyncStatus
import kotlinx.coroutines.flow.Flow

interface ObserveStravaSyncStatusUseCase {

    fun observe(sessionId: String): Flow<SessionSyncStatus>

    fun observeAll(): Flow<Map<String, SessionSyncStatus>>
}
