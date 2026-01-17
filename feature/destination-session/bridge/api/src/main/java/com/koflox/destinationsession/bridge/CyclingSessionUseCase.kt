package com.koflox.destinationsession.bridge

import kotlinx.coroutines.flow.StateFlow

/**
 * Domain-level interface for cycling session state.
 */
interface CyclingSessionUseCase {

    /**
     * Whether a session is currently active (running or paused).
     */
    val hasActiveSession: StateFlow<Boolean>
}
