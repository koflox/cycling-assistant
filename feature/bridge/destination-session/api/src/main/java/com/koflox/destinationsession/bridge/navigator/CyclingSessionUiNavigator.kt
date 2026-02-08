package com.koflox.destinationsession.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.location.model.Location

/**
 * UI-level interface for cycling session navigation and dialogs.
 * Provides Composable screens and dialogs for session interaction.
 */
interface CyclingSessionUiNavigator {

    /**
     * Returns the session screen Composable that handles all session UI.
     * This includes session stats, controls (pause/resume/stop).
     */
    @Composable
    fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
        onNavigateToCompletion: (sessionId: String) -> Unit,
    )

    /**
     * Returns the dialog Composable shown after destination selection.
     */
    @Composable
    fun DestinationOptions(
        destinationId: String,
        destinationName: String,
        destinationLocation: Location,
        distanceKm: Double,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    )

}
