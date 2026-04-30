package com.koflox.strava.impl.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strava_sync_status")
data class StravaSyncEntity(
    @PrimaryKey
    val sessionId: String,
    val state: String,
    val uploadId: Long?,
    val activityId: Long?,
    val errorReason: String?,
    val isRetryable: Boolean,
    val updatedAtMs: Long,
)
