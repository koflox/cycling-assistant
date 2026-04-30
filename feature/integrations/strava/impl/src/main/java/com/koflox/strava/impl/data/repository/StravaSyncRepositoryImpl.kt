package com.koflox.strava.impl.data.repository

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.impl.data.mapper.SessionSyncStatusMapper
import com.koflox.strava.impl.data.source.local.StravaSyncLocalDataSource
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class StravaSyncRepositoryImpl @Inject constructor(
    private val localDataSource: StravaSyncLocalDataSource,
    private val mapper: SessionSyncStatusMapper,
    private val timeProvider: CurrentTimeProvider,
) : StravaSyncRepository {

    override fun observe(sessionId: String): Flow<SessionSyncStatus> = localDataSource
        .observe(sessionId)
        .map { mapper.toDomain(it) }

    override fun observeAll(): Flow<Map<String, SessionSyncStatus>> = localDataSource
        .observeAll()
        .map { entities -> entities.associate { it.sessionId to mapper.toDomain(it) } }

    override suspend fun getStatus(sessionId: String): SessionSyncStatus =
        mapper.toDomain(localDataSource.get(sessionId))

    override suspend fun setStatus(sessionId: String, status: SessionSyncStatus) {
        localDataSource.upsert(mapper.toEntity(sessionId, status, timeProvider.currentTimeMs()))
    }

    override suspend fun setProcessing(sessionId: String, uploadId: Long) {
        localDataSource.upsert(
            mapper.toEntity(
                sessionId = sessionId,
                status = SessionSyncStatus.Processing,
                updatedAtMs = timeProvider.currentTimeMs(),
                uploadId = uploadId,
            ),
        )
    }

    override suspend fun getUploadId(sessionId: String): Long? =
        localDataSource.get(sessionId)?.uploadId

    override suspend fun clear(sessionId: String) {
        localDataSource.delete(sessionId)
    }
}
