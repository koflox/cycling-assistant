package com.koflox.strava.impl.domain.repository

import com.koflox.strava.api.model.SessionSyncStatus
import kotlinx.coroutines.flow.Flow

internal interface StravaSyncRepository {
    fun observe(sessionId: String): Flow<SessionSyncStatus>
    fun observeAll(): Flow<Map<String, SessionSyncStatus>>
    suspend fun getStatus(sessionId: String): SessionSyncStatus
    suspend fun setStatus(sessionId: String, status: SessionSyncStatus)
    suspend fun setProcessing(sessionId: String, uploadId: Long)
    suspend fun getUploadId(sessionId: String): Long?
    suspend fun clear(sessionId: String)
}
