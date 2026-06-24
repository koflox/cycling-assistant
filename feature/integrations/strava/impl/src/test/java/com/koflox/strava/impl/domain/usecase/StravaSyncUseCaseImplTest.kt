package com.koflox.strava.impl.domain.usecase

import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.domain.model.UploadStatus
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import com.koflox.strava.impl.work.StravaWorkScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StravaSyncUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-1"
        private const val UPLOAD_ID = 42L
        private const val ACTIVITY_ID = 999L
    }

    private val authRepository: StravaAuthRepository = mockk()
    private val syncRepository: StravaSyncRepository = mockk(relaxed = true)
    private val uploadRepository: StravaUploadRepository = mockk()
    private val workScheduler: StravaWorkScheduler = mockk(relaxed = true)

    private val useCase = StravaSyncUseCaseImpl(
        authRepository = authRepository,
        syncRepository = syncRepository,
        uploadRepository = uploadRepository,
        workScheduler = workScheduler,
    )

    @Test
    fun `enqueue marks AUTH_REQUIRED and skips scheduling when logged out`() = runTest {
        coEvery { authRepository.getCurrentAuthState() } returns StravaAuthState.LoggedOut

        useCase.enqueue(SESSION_ID)

        coVerify {
            syncRepository.setStatus(
                SESSION_ID,
                SessionSyncStatus.Error(SyncErrorReason.AUTH_REQUIRED, isRetryable = false),
            )
        }
        verify(exactly = 0) { workScheduler.enqueueUpload(any(), any()) }
    }

    @Test
    fun `enqueue marks Pending and schedules upload when logged in`() = runTest {
        coEvery { authRepository.getCurrentAuthState() } returns StravaAuthState.LoggedIn(1L, "John")

        useCase.enqueue(SESSION_ID)

        coVerify { syncRepository.setStatus(SESSION_ID, SessionSyncStatus.Pending) }
        verify { workScheduler.enqueueUpload(SESSION_ID) }
    }

    @Test
    fun `retry replaces existing upload work`() = runTest {
        coEvery { authRepository.getCurrentAuthState() } returns StravaAuthState.LoggedIn(1L, "John")

        useCase.retry(SESSION_ID)

        coVerify { syncRepository.setStatus(SESSION_ID, SessionSyncStatus.Pending) }
        verify { workScheduler.enqueueUpload(SESSION_ID, replace = true) }
    }

    @Test
    fun `retry without auth marks AUTH_REQUIRED`() = runTest {
        coEvery { authRepository.getCurrentAuthState() } returns StravaAuthState.LoggedOut

        useCase.retry(SESSION_ID)

        coVerify {
            syncRepository.setStatus(
                SESSION_ID,
                SessionSyncStatus.Error(SyncErrorReason.AUTH_REQUIRED, isRetryable = false),
            )
        }
        verify(exactly = 0) { workScheduler.enqueueUpload(any(), any()) }
    }

    @Test
    fun `refreshStatus marks Synced when upload is ready`() = runTest {
        coEvery { syncRepository.getUploadId(SESSION_ID) } returns UPLOAD_ID
        coEvery { uploadRepository.getUploadStatus(UPLOAD_ID) } returns
            Result.success(UploadStatus.Ready(uploadId = UPLOAD_ID, activityId = ACTIVITY_ID))

        useCase.refreshStatus(SESSION_ID)

        coVerify { syncRepository.setStatus(SESSION_ID, SessionSyncStatus.Synced(ACTIVITY_ID)) }
    }

    @Test
    fun `refreshStatus marks Processing and persists uploadId`() = runTest {
        coEvery { syncRepository.getUploadId(SESSION_ID) } returns UPLOAD_ID
        coEvery { uploadRepository.getUploadStatus(UPLOAD_ID) } returns
            Result.success(UploadStatus.Processing(uploadId = UPLOAD_ID))

        useCase.refreshStatus(SESSION_ID)

        coVerify { syncRepository.setProcessing(SESSION_ID, UPLOAD_ID) }
    }

    @Test
    fun `refreshStatus marks INVALID_ACTIVITY when upload failed`() = runTest {
        coEvery { syncRepository.getUploadId(SESSION_ID) } returns UPLOAD_ID
        coEvery { uploadRepository.getUploadStatus(UPLOAD_ID) } returns
            Result.success(UploadStatus.Failed(uploadId = UPLOAD_ID, message = "broken"))

        useCase.refreshStatus(SESSION_ID)

        coVerify {
            syncRepository.setStatus(
                SESSION_ID,
                SessionSyncStatus.Error(SyncErrorReason.INVALID_ACTIVITY, isRetryable = false),
            )
        }
    }

    @Test
    fun `refreshStatus does not mutate state on network failure`() = runTest {
        coEvery { syncRepository.getUploadId(SESSION_ID) } returns UPLOAD_ID
        coEvery { uploadRepository.getUploadStatus(UPLOAD_ID) } returns Result.failure(RuntimeException("boom"))

        useCase.refreshStatus(SESSION_ID)

        coVerify(exactly = 0) { syncRepository.setStatus(any(), any()) }
        coVerify(exactly = 0) { syncRepository.setProcessing(any(), any()) }
    }

    @Test
    fun `refreshStatus is no-op when uploadId is missing`() = runTest {
        coEvery { syncRepository.getUploadId(SESSION_ID) } returns null

        useCase.refreshStatus(SESSION_ID)

        coVerify(exactly = 0) { uploadRepository.getUploadStatus(any()) }
    }

    @Test
    fun `verifySyncedActivity clears local record when activity is gone`() = runTest {
        coEvery { syncRepository.getStatus(SESSION_ID) } returns SessionSyncStatus.Synced(ACTIVITY_ID)
        coEvery { uploadRepository.activityExists(ACTIVITY_ID) } returns Result.success(false)

        useCase.verifySyncedActivity(SESSION_ID)

        coVerify { syncRepository.clear(SESSION_ID) }
    }

    @Test
    fun `verifySyncedActivity keeps local record when activity exists`() = runTest {
        coEvery { syncRepository.getStatus(SESSION_ID) } returns SessionSyncStatus.Synced(ACTIVITY_ID)
        coEvery { uploadRepository.activityExists(ACTIVITY_ID) } returns Result.success(true)

        useCase.verifySyncedActivity(SESSION_ID)

        coVerify(exactly = 0) { syncRepository.clear(any()) }
    }

    @Test
    fun `verifySyncedActivity is silent on network failure`() = runTest {
        coEvery { syncRepository.getStatus(SESSION_ID) } returns SessionSyncStatus.Synced(ACTIVITY_ID)
        coEvery { uploadRepository.activityExists(ACTIVITY_ID) } returns Result.failure(RuntimeException("boom"))

        useCase.verifySyncedActivity(SESSION_ID)

        coVerify(exactly = 0) { syncRepository.clear(any()) }
    }

    @Test
    fun `verifySyncedActivity is no-op when not synced`() = runTest {
        coEvery { syncRepository.getStatus(SESSION_ID) } returns SessionSyncStatus.NotSynced

        useCase.verifySyncedActivity(SESSION_ID)

        coVerify(exactly = 0) { uploadRepository.activityExists(any()) }
        coVerify(exactly = 0) { syncRepository.clear(any()) }
    }
}
