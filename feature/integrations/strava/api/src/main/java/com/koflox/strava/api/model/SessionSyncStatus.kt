package com.koflox.strava.api.model

sealed interface SessionSyncStatus {

    data object NotSynced : SessionSyncStatus

    data object Pending : SessionSyncStatus

    data object Uploading : SessionSyncStatus

    data object Processing : SessionSyncStatus

    data class Synced(
        val activityId: Long,
    ) : SessionSyncStatus

    data class Error(
        val reason: SyncErrorReason,
        val isRetryable: Boolean,
    ) : SessionSyncStatus
}

enum class SyncErrorReason {
    NETWORK,
    AUTH_REQUIRED,
    RATE_LIMITED,
    INVALID_ACTIVITY,
    POLL_TIMEOUT,
    SERVER,
    UNKNOWN,
}
