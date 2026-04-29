package com.koflox.strava.impl.data.mapper

import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.data.source.local.entity.StravaSyncEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionSyncStatusMapperImplTest {

    companion object {
        private const val SESSION_ID = "session-1"
        private const val UPDATED_AT_MS = 1700000000000L
        private const val ACTIVITY_ID = 987654321L
    }

    private val mapper: SessionSyncStatusMapper = SessionSyncStatusMapperImpl()

    @Test
    fun `toDomain returns NotSynced for null entity`() {
        assertEquals(SessionSyncStatus.NotSynced, mapper.toDomain(null))
    }

    @Test
    fun `toDomain returns NotSynced for unknown state string`() {
        val entity = entity(state = "GIBBERISH")
        assertEquals(SessionSyncStatus.NotSynced, mapper.toDomain(entity))
    }

    @Test
    fun `toDomain maps PENDING state`() {
        assertEquals(SessionSyncStatus.Pending, mapper.toDomain(entity(state = "PENDING")))
    }

    @Test
    fun `toDomain maps UPLOADING state`() {
        assertEquals(SessionSyncStatus.Uploading, mapper.toDomain(entity(state = "UPLOADING")))
    }

    @Test
    fun `toDomain maps PROCESSING state`() {
        assertEquals(SessionSyncStatus.Processing, mapper.toDomain(entity(state = "PROCESSING")))
    }

    @Test
    fun `toDomain maps SYNCED state with activityId`() {
        val entity = entity(state = "SYNCED", activityId = ACTIVITY_ID)
        assertEquals(SessionSyncStatus.Synced(ACTIVITY_ID), mapper.toDomain(entity))
    }

    @Test
    fun `toDomain returns Synced with zero activityId when null`() {
        val entity = entity(state = "SYNCED", activityId = null)
        assertEquals(SessionSyncStatus.Synced(0L), mapper.toDomain(entity))
    }

    @Test
    fun `toDomain maps ERROR with known reason and retryable flag`() {
        val entity = entity(state = "ERROR", errorReason = "RATE_LIMITED", isRetryable = true)
        assertEquals(
            SessionSyncStatus.Error(SyncErrorReason.RATE_LIMITED, isRetryable = true),
            mapper.toDomain(entity),
        )
    }

    @Test
    fun `toDomain maps ERROR with unknown reason to UNKNOWN`() {
        val entity = entity(state = "ERROR", errorReason = "GIBBERISH", isRetryable = false)
        assertEquals(
            SessionSyncStatus.Error(SyncErrorReason.UNKNOWN, isRetryable = false),
            mapper.toDomain(entity),
        )
    }

    @Test
    fun `toDomain maps ERROR with null reason to UNKNOWN`() {
        val entity = entity(state = "ERROR", errorReason = null, isRetryable = false)
        assertEquals(
            SessionSyncStatus.Error(SyncErrorReason.UNKNOWN, isRetryable = false),
            mapper.toDomain(entity),
        )
    }

    @Test
    fun `toEntity maps NotSynced`() {
        val entity = mapper.toEntity(SESSION_ID, SessionSyncStatus.NotSynced, UPDATED_AT_MS)
        assertEquals(entity(state = "NOT_SYNCED"), entity)
    }

    @Test
    fun `toEntity maps Pending`() {
        val entity = mapper.toEntity(SESSION_ID, SessionSyncStatus.Pending, UPDATED_AT_MS)
        assertEquals(entity(state = "PENDING"), entity)
    }

    @Test
    fun `toEntity maps Uploading`() {
        val entity = mapper.toEntity(SESSION_ID, SessionSyncStatus.Uploading, UPDATED_AT_MS)
        assertEquals(entity(state = "UPLOADING"), entity)
    }

    @Test
    fun `toEntity maps Processing`() {
        val entity = mapper.toEntity(SESSION_ID, SessionSyncStatus.Processing, UPDATED_AT_MS)
        assertEquals(entity(state = "PROCESSING"), entity)
    }

    @Test
    fun `toEntity maps Synced`() {
        val entity = mapper.toEntity(SESSION_ID, SessionSyncStatus.Synced(ACTIVITY_ID), UPDATED_AT_MS)
        assertEquals(entity(state = "SYNCED", activityId = ACTIVITY_ID), entity)
    }

    @Test
    fun `toEntity maps Error with reason and retryable flag`() {
        val status = SessionSyncStatus.Error(SyncErrorReason.AUTH_REQUIRED, isRetryable = false)
        val entity = mapper.toEntity(SESSION_ID, status, UPDATED_AT_MS)
        assertEquals(
            entity(state = "ERROR", errorReason = "AUTH_REQUIRED", isRetryable = false),
            entity,
        )
    }

    @Test
    fun `entity to domain to entity round-trip preserves all states`() {
        val statuses = listOf(
            SessionSyncStatus.NotSynced,
            SessionSyncStatus.Pending,
            SessionSyncStatus.Uploading,
            SessionSyncStatus.Processing,
            SessionSyncStatus.Synced(ACTIVITY_ID),
            SessionSyncStatus.Error(SyncErrorReason.NETWORK, isRetryable = true),
        )
        statuses.forEach { status ->
            val entity = mapper.toEntity(SESSION_ID, status, UPDATED_AT_MS)
            assertEquals(status, mapper.toDomain(entity))
        }
    }

    private fun entity(
        state: String,
        activityId: Long? = null,
        errorReason: String? = null,
        isRetryable: Boolean = false,
        uploadId: Long? = null,
    ) = StravaSyncEntity(
        sessionId = SESSION_ID,
        state = state,
        uploadId = uploadId,
        activityId = activityId,
        errorReason = errorReason,
        isRetryable = isRetryable,
        updatedAtMs = UPDATED_AT_MS,
    )
}
