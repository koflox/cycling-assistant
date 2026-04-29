package com.koflox.strava.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koflox.gpx.GpxInput
import com.koflox.gpx.GpxMapper
import com.koflox.sessionstrava.bridge.SessionGpxDataProvider
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.domain.model.StravaError
import com.koflox.strava.impl.domain.model.UploadStatus
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class StravaUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: StravaAuthRepository,
    private val syncRepository: StravaSyncRepository,
    private val uploadRepository: StravaUploadRepository,
    private val sessionGpxDataProvider: SessionGpxDataProvider,
    private val gpxMapper: GpxMapper,
    private val workScheduler: StravaWorkScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID)
        return when {
            sessionId == null -> Result.failure()
            else -> runUpload(sessionId)
        }
    }

    private suspend fun runUpload(sessionId: String): Result {
        val gpxInput = prepareGpxInput(sessionId) ?: return Result.failure()
        syncRepository.setStatus(sessionId, SessionSyncStatus.Uploading)
        val gpxBytes = gpxMapper.map(gpxInput).toByteArray(Charsets.UTF_8)
        return uploadRepository.uploadGpx(
            gpxBytes = gpxBytes,
            externalId = sessionId,
            name = activityName(gpxInput),
        ).fold(
            onSuccess = { handleUploadSuccess(sessionId, it) },
            onFailure = { handleUploadFailure(sessionId, it) },
        )
    }

    private suspend fun prepareGpxInput(sessionId: String): GpxInput? {
        val authState = authRepository.getCurrentAuthState()
        return when {
            authState !is StravaAuthState.LoggedIn -> {
                syncRepository.setStatus(
                    sessionId,
                    SessionSyncStatus.Error(SyncErrorReason.AUTH_REQUIRED, isRetryable = false),
                )
                null
            }
            else -> validateGpx(sessionId)
        }
    }

    private suspend fun validateGpx(sessionId: String): GpxInput? {
        val gpxInput = sessionGpxDataProvider.getGpxInput(sessionId).getOrNull()
        return if (gpxInput == null || gpxInput.trackPoints.isEmpty()) {
            syncRepository.setStatus(
                sessionId,
                SessionSyncStatus.Error(SyncErrorReason.INVALID_ACTIVITY, isRetryable = false),
            )
            null
        } else {
            gpxInput
        }
    }

    private suspend fun handleUploadSuccess(sessionId: String, status: UploadStatus): Result =
        when (status) {
            is UploadStatus.Ready -> {
                syncRepository.setStatus(sessionId, SessionSyncStatus.Synced(status.activityId))
                Result.success()
            }
            is UploadStatus.Processing -> {
                syncRepository.setProcessing(sessionId, status.uploadId)
                workScheduler.enqueuePoll(sessionId, status.uploadId)
                Result.success()
            }
            is UploadStatus.Failed -> {
                syncRepository.setStatus(
                    sessionId,
                    SessionSyncStatus.Error(SyncErrorReason.INVALID_ACTIVITY, isRetryable = false),
                )
                Result.success()
            }
        }

    private suspend fun handleUploadFailure(sessionId: String, throwable: Throwable): Result {
        val error = throwable as? StravaError ?: StravaError.Unknown()
        return if (error.isRetryable) {
            Result.retry()
        } else {
            syncRepository.setStatus(
                sessionId,
                SessionSyncStatus.Error(error.reason, isRetryable = false),
            )
            Result.success()
        }
    }

    private fun activityName(input: GpxInput): String = input.name

    companion object {
        const val KEY_SESSION_ID = "session_id"
    }
}
