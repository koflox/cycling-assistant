package com.koflox.destinationsession.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.location.model.Location
import kotlinx.coroutines.flow.StateFlow

// TODO: split into Ui bridge and DomainBridge
interface DestinationSessionBridge {

    /**
     * Whether a session is currently active (running or paused).
     */
    val hasActiveSession: StateFlow<Boolean>

    /**
     * Returns the session screen Composable that handles all session UI.
     * This includes session stats, controls (pause/resume/stop).
     */
    @Composable
    fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
    )

    /**
     * Returns the confirmation dialog Composable shown after destination selection.
     */
    @Composable
    fun ConfirmationDialog(
        destinationId: String,
        destinationName: String,
        destinationLocation: Location,
        distanceKm: Double,
        userLocation: Location,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    )
}
