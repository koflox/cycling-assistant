package com.koflox.strava.api.usecase

interface StravaSyncUseCase {

    suspend fun enqueue(sessionId: String)

    suspend fun retry(sessionId: String)

    suspend fun refreshStatus(sessionId: String)

    /**
     * For a session whose local state is `Synced`, checks Strava that the activity still
     * exists. If Strava returns 404 (activity was deleted on the web/app), the local sync
     * record is cleared so the UI falls back to `NotSynced`. Network/auth errors are
     * silently ignored — we only react to a definitive "gone".
     */
    suspend fun verifySyncedActivity(sessionId: String)
}
