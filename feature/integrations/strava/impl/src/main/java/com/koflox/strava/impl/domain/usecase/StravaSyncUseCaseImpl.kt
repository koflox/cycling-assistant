package com.koflox.strava.impl.domain.usecase

import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.api.usecase.StravaSyncUseCase
import com.koflox.strava.impl.domain.model.UploadStatus
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import com.koflox.strava.impl.work.StravaWorkScheduler
import javax.inject.Inject

internal class StravaSyncUseCaseImpl @Inject constructor(
    private val authRepository: StravaAuthRepository,
    private val syncRepository: StravaSyncRepository,
    private val uploadRepository: StravaUploadRepository,
    private val workScheduler: StravaWorkScheduler,
) : StravaSyncUseCase {

    override suspend fun enqueue(sessionId: String) {
        if (authRepository.getCurrentAuthState() !is StravaAuthState.LoggedIn) {
            syncRepository.setStatus(
                sessionId,
                SessionSyncStatus.Error(SyncErrorReason.AUTH_REQUIRED, isRetryable = false),
            )
            return
        }
        syncRepository.setStatus(sessionId, SessionSyncStatus.Pending)
        workScheduler.enqueueUpload(sessionId)
    }

    override suspend fun retry(sessionId: String) {
        if (authRepository.getCurrentAuthState() !is StravaAuthState.LoggedIn) {
            syncRepository.setStatus(
                sessionId,
                SessionSyncStatus.Error(SyncErrorReason.AUTH_REQUIRED, isRetryable = false),
            )
            return
        }
        syncRepository.setStatus(sessionId, SessionSyncStatus.Pending)
        workScheduler.enqueueUpload(sessionId, replace = true)
    }

    override suspend fun refreshStatus(sessionId: String) {
        internalRefreshStatus(sessionId)
    }

    override suspend fun reconcileStatus(sessionId: String) {
        when (val current = syncRepository.getStatus(sessionId)) {
            is SessionSyncStatus.Synced -> verifyActivityExists(sessionId, current.activityId)
            SessionSyncStatus.Processing,
            is SessionSyncStatus.Error,
            -> internalRefreshStatus(sessionId)
            else -> Unit
        }
    }

    private suspend fun internalRefreshStatus(sessionId: String) {
        val uploadId = syncRepository.getUploadId(sessionId) ?: return
        uploadRepository.getUploadStatus(uploadId).onSuccess { status ->
            applyUploadStatus(sessionId, status)
        }
    }

    private suspend fun verifyActivityExists(sessionId: String, activityId: Long) {
        uploadRepository.activityExists(activityId).onSuccess { exists ->
            if (!exists) {
                syncRepository.clear(sessionId)
            }
        }
    }

    private suspend fun applyUploadStatus(sessionId: String, status: UploadStatus) {
        when (status) {
            is UploadStatus.Ready -> syncRepository.setStatus(sessionId, SessionSyncStatus.Synced(status.activityId))
            is UploadStatus.Processing -> syncRepository.setProcessing(sessionId, status.uploadId)
            is UploadStatus.Failed -> syncRepository.setStatus(
                sessionId,
                SessionSyncStatus.Error(SyncErrorReason.INVALID_ACTIVITY, isRetryable = false),
            )
        }
    }
}
