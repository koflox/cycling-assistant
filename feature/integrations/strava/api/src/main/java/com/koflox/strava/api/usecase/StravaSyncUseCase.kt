package com.koflox.strava.api.usecase

interface StravaSyncUseCase {

    suspend fun enqueue(sessionId: String)

    suspend fun retry(sessionId: String)

    suspend fun refreshStatus(sessionId: String)

    /**
     * Reconciles the locally-stored sync state with Strava when the share tab is opened:
     *  - `Synced`: verifies the activity still exists. If Strava returns 404 (activity was
     *    deleted on the web/app), the local record is cleared so the UI falls back to
     *    `NotSynced`. Network/auth errors are silently ignored — we only react to a
     *    definitive "gone".
     *  - `Processing` / retryable `Error` (e.g. a poll that timed out while the app was
     *    backgrounded): re-queries the upload status so an upload Strava finished in the
     *    meantime resolves to `Synced`, instead of leaving the UI on a stale in-progress or
     *    error state that would force a duplicate re-upload.
     */
    suspend fun reconcileStatus(sessionId: String)
}
