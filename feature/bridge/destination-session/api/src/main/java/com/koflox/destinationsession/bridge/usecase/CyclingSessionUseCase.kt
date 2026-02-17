package com.koflox.destinationsession.bridge.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Domain-level interface for cycling session state observation.
 */
interface CyclingSessionUseCase {

    /**
     * Observe whether a session is currently active (running or paused).
     */
    fun observeHasActiveSession(): Flow<Boolean>

    /**
     * Get the destination of the active session.
     * Returns null when there's no active session or the session is free roam.
     */
    suspend fun getActiveSessionDestination(): ActiveSessionDestination?

}
