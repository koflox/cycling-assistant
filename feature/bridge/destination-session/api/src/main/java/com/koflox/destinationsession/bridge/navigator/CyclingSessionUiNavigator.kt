package com.koflox.destinationsession.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.location.model.Location

/**
 * UI-level interface for cycling session navigation and dialogs.
 * Provides Composable screens and dialogs for session interaction.
 *
 * Both [DestinationOptions] and [StartFreeRoamSession] handle notification permission
 * (POST_NOTIFICATIONS on Android 13+) internally before starting the session,
 * ensuring a consistent pre-session flow regardless of riding mode.
 */
interface CyclingSessionUiNavigator {

    /**
     * Returns the session screen Composable that handles all session UI.
     * This includes session stats, controls (pause/resume/stop).
     */
    @Composable
    fun SessionScreen(
        destinationLocation: Location?,
        modifier: Modifier,
        onNavigateToCompletion: (sessionId: String) -> Unit,
    )

    /**
     * Handles the free roam session start flow:
     * requests notification permission, then starts a free roam session.
     *
     * Mirrors [DestinationOptions] which performs the same pre-session steps
     * for destination-based sessions.
     */
    @Composable
    fun StartFreeRoamSession()

    /**
     * Returns the dialog Composable shown after destination selection.
     * Handles notification permission and session start internally.
     *
     * @param onSessionStarting called when session creation is imminent (after permission handling).
     */
    @Composable
    fun DestinationOptions(
        destinationId: String,
        destinationName: String,
        destinationLocation: Location,
        distanceKm: Double,
        isNavigateVisible: Boolean,
        onSessionStarting: () -> Unit,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    )

}
