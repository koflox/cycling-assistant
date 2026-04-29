package com.koflox.strava.impl.data.repository

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.data.mapper.SessionSyncStatusMapper
import com.koflox.strava.impl.data.source.local.StravaSyncLocalDataSource
import com.koflox.strava.impl.data.source.local.entity.StravaSyncEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StravaSyncRepositoryImplTest {

    companion object {
        private const val SESSION_ID = "session-1"
        private const val OTHER_SESSION_ID = "session-2"
        private const val UPDATED_AT_MS = 1700000000000L
        private const val ACTIVITY_ID = 999L
    }

    private val localDataSource: StravaSyncLocalDataSource = mockk()
    private val mapper: SessionSyncStatusMapper = mockk()
    private val timeProvider: CurrentTimeProvider = mockk()

    private val repository = StravaSyncRepositoryImpl(
        localDataSource = localDataSource,
        mapper = mapper,
        timeProvider = timeProvider,
    )

    @Test
    fun `observe maps DAO entity to domain status`() = runTest {
        val entity = entity(state = "PROCESSING")
        every { localDataSource.observe(SESSION_ID) } returns flowOf(entity)
        every { mapper.toDomain(entity) } returns SessionSyncStatus.Processing

        val result = repository.observe(SESSION_ID).first()

        assertEquals(SessionSyncStatus.Processing, result)
    }

    @Test
    fun `observe maps null entity to NotSynced`() = runTest {
        every { localDataSource.observe(SESSION_ID) } returns flowOf(null)
        every { mapper.toDomain(null) } returns SessionSyncStatus.NotSynced

        val result = repository.observe(SESSION_ID).first()

        assertEquals(SessionSyncStatus.NotSynced, result)
    }

    @Test
    fun `observeAll associates entities by sessionId`() = runTest {
        val entity1 = entity(sessionId = SESSION_ID, state = "SYNCED", activityId = ACTIVITY_ID)
        val entity2 = entity(sessionId = OTHER_SESSION_ID, state = "ERROR")
        every { localDataSource.observeAll() } returns flowOf(listOf(entity1, entity2))
        every { mapper.toDomain(entity1) } returns SessionSyncStatus.Synced(ACTIVITY_ID)
        every { mapper.toDomain(entity2) } returns SessionSyncStatus.Error(SyncErrorReason.UNKNOWN, isRetryable = false)

        val result = repository.observeAll().first()

        assertEquals(2, result.size)
        assertEquals(SessionSyncStatus.Synced(ACTIVITY_ID), result[SESSION_ID])
        assertEquals(SessionSyncStatus.Error(SyncErrorReason.UNKNOWN, isRetryable = false), result[OTHER_SESSION_ID])
    }

    @Test
    fun `setStatus maps with current time and upserts`() = runTest {
        val status = SessionSyncStatus.Pending
        val mapped = entity(state = "PENDING")
        every { timeProvider.currentTimeMs() } returns UPDATED_AT_MS
        every { mapper.toEntity(SESSION_ID, status, UPDATED_AT_MS, null) } returns mapped
        coEvery { localDataSource.upsert(mapped) } returns Unit

        repository.setStatus(SESSION_ID, status)

        coVerify { localDataSource.upsert(mapped) }
    }

    @Test
    fun `clear delegates to local data source`() = runTest {
        coEvery { localDataSource.delete(SESSION_ID) } returns Unit

        repository.clear(SESSION_ID)

        coVerify { localDataSource.delete(SESSION_ID) }
    }

    @Test
    fun `setProcessing maps with Processing status and uploadId`() = runTest {
        val mapped = entity(state = "PROCESSING", uploadId = 42L)
        every { timeProvider.currentTimeMs() } returns UPDATED_AT_MS
        every {
            mapper.toEntity(SESSION_ID, SessionSyncStatus.Processing, UPDATED_AT_MS, 42L)
        } returns mapped
        coEvery { localDataSource.upsert(mapped) } returns Unit

        repository.setProcessing(SESSION_ID, 42L)

        coVerify { localDataSource.upsert(mapped) }
    }

    @Test
    fun `getUploadId returns uploadId from entity`() = runTest {
        coEvery { localDataSource.get(SESSION_ID) } returns entity(state = "PROCESSING", uploadId = 42L)

        val result = repository.getUploadId(SESSION_ID)

        assertEquals(42L, result)
    }

    @Test
    fun `getUploadId returns null when entity missing`() = runTest {
        coEvery { localDataSource.get(SESSION_ID) } returns null

        val result = repository.getUploadId(SESSION_ID)

        assertEquals(null, result)
    }

    private fun entity(
        sessionId: String = SESSION_ID,
        state: String = "PENDING",
        activityId: Long? = null,
        errorReason: String? = null,
        isRetryable: Boolean = false,
        uploadId: Long? = null,
    ) = StravaSyncEntity(
        sessionId = sessionId,
        state = state,
        uploadId = uploadId,
        activityId = activityId,
        errorReason = errorReason,
        isRetryable = isRetryable,
        updatedAtMs = UPDATED_AT_MS,
    )
}
