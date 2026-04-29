package com.koflox.strava.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.domain.model.StravaError
import com.koflox.strava.impl.domain.model.UploadStatus
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class StravaPollWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: StravaSyncRepository,
    private val uploadRepository: StravaUploadRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID)
        val uploadId = inputData.getLong(KEY_UPLOAD_ID, -1L).takeIf { it >= 0 }
        return if (sessionId == null || uploadId == null) {
            Result.failure()
        } else {
            uploadRepository.getUploadStatus(uploadId).fold(
                onSuccess = { handlePollSuccess(sessionId, it) },
                onFailure = { handlePollFailure(sessionId, it) },
            )
        }
    }

    private suspend fun handlePollSuccess(sessionId: String, status: UploadStatus): Result =
        when (status) {
            is UploadStatus.Ready -> {
                syncRepository.setStatus(sessionId, SessionSyncStatus.Synced(status.activityId))
                Result.success()
            }
            is UploadStatus.Processing -> {
                if (runAttemptCount + 1 >= StravaWorkScheduler.MAX_POLL_ATTEMPTS) {
                    syncRepository.setStatus(
                        sessionId,
                        SessionSyncStatus.Error(SyncErrorReason.POLL_TIMEOUT, isRetryable = true),
                    )
                    Result.success()
                } else {
                    Result.retry()
                }
            }
            is UploadStatus.Failed -> {
                syncRepository.setStatus(
                    sessionId,
                    SessionSyncStatus.Error(SyncErrorReason.INVALID_ACTIVITY, isRetryable = false),
                )
                Result.success()
            }
        }

    private suspend fun handlePollFailure(sessionId: String, throwable: Throwable): Result {
        val error = throwable as? StravaError ?: StravaError.Unknown()
        return if (!error.isRetryable || runAttemptCount + 1 >= StravaWorkScheduler.MAX_POLL_ATTEMPTS) {
            syncRepository.setStatus(
                sessionId,
                SessionSyncStatus.Error(error.reason, isRetryable = error.isRetryable),
            )
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_UPLOAD_ID = "upload_id"
    }
}
