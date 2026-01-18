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
        onNavigateToCompletion: (sessionId: String) -> Unit,
        modifier: Modifier,
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
        userLocation: Location,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    )

    /**
     * Returns the sessions list screen Composable.
     */
    @Composable
    fun SessionsScreen(
        onBackClick: () -> Unit,
        onSessionClick: (sessionId: String) -> Unit,
        modifier: Modifier,
    )

    /**
     * Returns the session completion screen Composable.
     */
    @Composable
    fun SessionCompletionScreen(
        onBackClick: () -> Unit,
        onNavigateToDashboard: () -> Unit,
        modifier: Modifier,
    )
}
