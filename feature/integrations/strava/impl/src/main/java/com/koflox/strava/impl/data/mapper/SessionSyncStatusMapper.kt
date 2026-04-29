package com.koflox.strava.impl.data.mapper

import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.data.source.local.entity.StravaSyncEntity

internal interface SessionSyncStatusMapper {
    fun toDomain(entity: StravaSyncEntity?): SessionSyncStatus
    fun toEntity(
        sessionId: String,
        status: SessionSyncStatus,
        updatedAtMs: Long,
        uploadId: Long? = null,
    ): StravaSyncEntity
}

internal class SessionSyncStatusMapperImpl : SessionSyncStatusMapper {

    override fun toDomain(entity: StravaSyncEntity?): SessionSyncStatus {
        if (entity == null) return SessionSyncStatus.NotSynced
        return when (entity.state) {
            STATE_PENDING -> SessionSyncStatus.Pending
            STATE_UPLOADING -> SessionSyncStatus.Uploading
            STATE_PROCESSING -> SessionSyncStatus.Processing
            STATE_SYNCED -> SessionSyncStatus.Synced(entity.activityId ?: 0L)
            STATE_ERROR -> SessionSyncStatus.Error(
                reason = entity.errorReason?.let { runCatching { SyncErrorReason.valueOf(it) }.getOrNull() }
                    ?: SyncErrorReason.UNKNOWN,
                isRetryable = entity.isRetryable,
            )
            else -> SessionSyncStatus.NotSynced
        }
    }

    override fun toEntity(
        sessionId: String,
        status: SessionSyncStatus,
        updatedAtMs: Long,
        uploadId: Long?,
    ): StravaSyncEntity = when (status) {
        SessionSyncStatus.NotSynced -> entityWith(sessionId, STATE_NOT_SYNCED, updatedAtMs, uploadId = uploadId)
        SessionSyncStatus.Pending -> entityWith(sessionId, STATE_PENDING, updatedAtMs, uploadId = uploadId)
        SessionSyncStatus.Uploading -> entityWith(sessionId, STATE_UPLOADING, updatedAtMs, uploadId = uploadId)
        SessionSyncStatus.Processing -> entityWith(sessionId, STATE_PROCESSING, updatedAtMs, uploadId = uploadId)
        is SessionSyncStatus.Synced -> entityWith(
            sessionId = sessionId,
            state = STATE_SYNCED,
            updatedAtMs = updatedAtMs,
            uploadId = uploadId,
            activityId = status.activityId,
        )
        is SessionSyncStatus.Error -> entityWith(
            sessionId = sessionId,
            state = STATE_ERROR,
            updatedAtMs = updatedAtMs,
            uploadId = uploadId,
            errorReason = status.reason.name,
            isRetryable = status.isRetryable,
        )
    }

    @Suppress("LongParameterList")
    private fun entityWith(
        sessionId: String,
        state: String,
        updatedAtMs: Long,
        uploadId: Long? = null,
        activityId: Long? = null,
        errorReason: String? = null,
        isRetryable: Boolean = false,
    ) = StravaSyncEntity(
        sessionId = sessionId,
        state = state,
        uploadId = uploadId,
        activityId = activityId,
        errorReason = errorReason,
        isRetryable = isRetryable,
        updatedAtMs = updatedAtMs,
    )

    private companion object {
        const val STATE_NOT_SYNCED = "NOT_SYNCED"
        const val STATE_PENDING = "PENDING"
        const val STATE_UPLOADING = "UPLOADING"
        const val STATE_PROCESSING = "PROCESSING"
        const val STATE_SYNCED = "SYNCED"
        const val STATE_ERROR = "ERROR"
    }
}
