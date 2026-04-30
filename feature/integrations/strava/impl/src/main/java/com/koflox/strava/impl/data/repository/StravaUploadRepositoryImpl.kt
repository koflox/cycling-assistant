package com.koflox.strava.impl.data.repository

import com.koflox.concurrent.suspendRunCatching
import com.koflox.strava.impl.data.mapper.StravaErrorMapper
import com.koflox.strava.impl.data.mapper.UploadStatusMapper
import com.koflox.strava.impl.data.source.remote.StravaUploadRemoteDataSource
import com.koflox.strava.impl.domain.model.UploadStatus
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import javax.inject.Inject

internal class StravaUploadRepositoryImpl @Inject constructor(
    private val remoteDataSource: StravaUploadRemoteDataSource,
    private val uploadStatusMapper: UploadStatusMapper,
    private val errorMapper: StravaErrorMapper,
) : StravaUploadRepository {

    override suspend fun uploadGpx(
        gpxBytes: ByteArray,
        externalId: String,
        name: String,
    ): Result<UploadStatus> = suspendRunCatching {
        uploadStatusMapper.toDomain(remoteDataSource.uploadGpx(gpxBytes, externalId, name))
    }.recoverCatching { throw errorMapper.map(it) }

    override suspend fun getUploadStatus(uploadId: Long): Result<UploadStatus> = suspendRunCatching {
        uploadStatusMapper.toDomain(remoteDataSource.getUploadStatus(uploadId))
    }.recoverCatching { throw errorMapper.map(it) }

    override suspend fun activityExists(activityId: Long): Result<Boolean> = suspendRunCatching {
        remoteDataSource.doesActivityExist(activityId)
    }.recoverCatching { throw errorMapper.map(it) }
}
