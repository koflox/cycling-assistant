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
}
