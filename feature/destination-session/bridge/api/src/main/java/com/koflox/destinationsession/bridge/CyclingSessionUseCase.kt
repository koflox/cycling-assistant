package com.koflox.destinationsession.bridge

import kotlinx.coroutines.flow.Flow

/**
 * Domain-level interface for cycling session state.
 */
interface CyclingSessionUseCase {

    /**
     * Observe whether a session is currently active (running or paused).
     */
    fun observeHasActiveSession(): Flow<Boolean>

    /**
     * Get the destination of the active session.
     * Returns null when there's no active session.
     */
    suspend fun getActiveSessionDestination(): ActiveSessionDestination?

}
