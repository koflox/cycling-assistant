package com.koflox.strava.impl.domain.model

internal sealed interface UploadStatus {
    val uploadId: Long

    data class Processing(override val uploadId: Long) : UploadStatus

    data class Ready(
        override val uploadId: Long,
        val activityId: Long,
    ) : UploadStatus

    data class Failed(
        override val uploadId: Long,
        val message: String,
    ) : UploadStatus
}
