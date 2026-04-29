package com.koflox.strava.impl.domain.repository

import com.koflox.strava.impl.domain.model.UploadStatus

internal interface StravaUploadRepository {
    suspend fun uploadGpx(gpxBytes: ByteArray, externalId: String, name: String): Result<UploadStatus>
    suspend fun getUploadStatus(uploadId: Long): Result<UploadStatus>
    suspend fun activityExists(activityId: Long): Result<Boolean>
}
