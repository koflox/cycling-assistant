package com.koflox.strava.impl.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

internal class StravaWorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun enqueueUpload(sessionId: String, replace: Boolean = false) {
        val request = uploadRequest(sessionId)
        WorkManager.getInstance(context).enqueueUniqueWork(
            uploadWorkName(sessionId),
            if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueuePoll(sessionId: String, uploadId: Long) {
        val request = pollRequest(sessionId, uploadId)
        WorkManager.getInstance(context).enqueueUniqueWork(
            pollWorkName(sessionId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun uploadRequest(sessionId: String): OneTimeWorkRequest {
        val data = Data.Builder()
            .putString(StravaUploadWorker.KEY_SESSION_ID, sessionId)
            .build()
        return OneTimeWorkRequestBuilder<StravaUploadWorker>()
            .setInputData(data)
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, UPLOAD_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .addTag(TAG_STRAVA_SYNC)
            .build()
    }

    private fun pollRequest(sessionId: String, uploadId: Long): OneTimeWorkRequest {
        val data = Data.Builder()
            .putString(StravaPollWorker.KEY_SESSION_ID, sessionId)
            .putLong(StravaPollWorker.KEY_UPLOAD_ID, uploadId)
            .build()
        return OneTimeWorkRequestBuilder<StravaPollWorker>()
            .setInputData(data)
            .setConstraints(networkConstraints)
            .setInitialDelay(POLL_INITIAL_DELAY.inWholeSeconds, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, POLL_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .addTag(TAG_STRAVA_SYNC)
            .build()
    }

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        const val MAX_POLL_ATTEMPTS = 5
        const val TAG_STRAVA_SYNC = "strava-sync"
        private val POLL_INITIAL_DELAY = 10.seconds
        private const val UPLOAD_BACKOFF_SECONDS = 30L
        private const val POLL_BACKOFF_SECONDS = 10L

        fun uploadWorkName(sessionId: String): String = "strava-upload-$sessionId"
        fun pollWorkName(sessionId: String): String = "strava-poll-$sessionId"
    }
}
