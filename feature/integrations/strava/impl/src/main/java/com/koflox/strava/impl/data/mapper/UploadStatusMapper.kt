package com.koflox.strava.impl.data.mapper

import com.koflox.strava.impl.data.api.dto.UploadResponse
import com.koflox.strava.impl.domain.model.UploadStatus

internal interface UploadStatusMapper {
    fun toDomain(response: UploadResponse): UploadStatus
    fun extractDuplicateActivityId(error: String): Long?
}

internal class UploadStatusMapperImpl : UploadStatusMapper {

    override fun toDomain(response: UploadResponse): UploadStatus {
        val errorMessage = response.error
        if (!errorMessage.isNullOrBlank()) {
            val duplicateId = extractDuplicateActivityId(errorMessage)
            return if (duplicateId != null) {
                UploadStatus.Ready(uploadId = response.id, activityId = duplicateId)
            } else {
                UploadStatus.Failed(uploadId = response.id, message = errorMessage)
            }
        }
        val activityId = response.activityId
        return if (activityId != null) {
            UploadStatus.Ready(uploadId = response.id, activityId = activityId)
        } else {
            UploadStatus.Processing(uploadId = response.id)
        }
    }

    override fun extractDuplicateActivityId(error: String): Long? =
        DUPLICATE_REGEX.find(error)?.groupValues?.getOrNull(1)?.toLongOrNull()

    private companion object {
        val DUPLICATE_REGEX = Regex("duplicate of activity (\\d+)")
    }
}
