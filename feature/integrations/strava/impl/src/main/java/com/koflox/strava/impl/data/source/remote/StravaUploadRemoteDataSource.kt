package com.koflox.strava.impl.data.source.remote

import com.koflox.di.IoDispatcher
import com.koflox.strava.impl.data.api.StravaUploadApi
import com.koflox.strava.impl.data.api.dto.UploadResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface StravaUploadRemoteDataSource {
    suspend fun uploadGpx(gpxBytes: ByteArray, externalId: String, name: String): UploadResponse
    suspend fun getUploadStatus(uploadId: Long): UploadResponse
    suspend fun doesActivityExist(activityId: Long): Boolean
}

internal class StravaUploadRemoteDataSourceImpl @Inject constructor(
    private val api: StravaUploadApi,
    @param:IoDispatcher private val dispatcherIo: CoroutineDispatcher,
) : StravaUploadRemoteDataSource {

    override suspend fun uploadGpx(
        gpxBytes: ByteArray,
        externalId: String,
        name: String,
    ): UploadResponse = withContext(dispatcherIo) {
        api.uploadGpx(gpxBytes, externalId, name)
    }

    override suspend fun getUploadStatus(uploadId: Long): UploadResponse = withContext(dispatcherIo) {
        api.getUploadStatus(uploadId)
    }

    override suspend fun doesActivityExist(activityId: Long): Boolean = withContext(dispatcherIo) {
        api.activityExists(activityId)
    }
}
