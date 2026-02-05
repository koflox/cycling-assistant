package com.koflox.session.presentation.session

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.koflox.location.settings.LocationSettingsHandler
import com.koflox.session.presentation.dialog.LocationDisabledDialog
import com.koflox.session.presentation.dialog.StopConfirmationDialog
import com.koflox.session.presentation.session.components.SessionControlsOverlay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SessionScreenRoute(
    onNavigateToCompletion: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SessionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                is SessionNavigation.ToCompletion -> onNavigateToCompletion(event.sessionId)
            }
        }
    }
    LaunchedEffect(uiState) {
        val active = uiState as? SessionUiState.Active ?: return@LaunchedEffect
        if (active.overlay is SessionOverlay.Error) {
            Toast.makeText(context, active.overlay.message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SessionUiEvent.SessionManagementEvent.ErrorDismissed)
        }
    }
    SessionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun SessionContent(
    uiState: SessionUiState,
    onEvent: (SessionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldResolveLocation by remember { mutableStateOf(false) }
    if (shouldResolveLocation) {
        LocationSettingsHandler(
            onLocationEnabled = {
                shouldResolveLocation = false
                onEvent(SessionUiEvent.LocationSettingsEvent.LocationEnabled)
            },
            onLocationDenied = {
                shouldResolveLocation = false
                onEvent(SessionUiEvent.LocationSettingsEvent.LocationEnableDenied)
            },
        )
    }
    Box(modifier = modifier) {
        when (uiState) {
            SessionUiState.Idle -> Unit
            is SessionUiState.Active -> {
                SessionControlsOverlay(
                    state = uiState,
                    onPauseClick = { onEvent(SessionUiEvent.SessionManagementEvent.PauseClicked) },
                    onResumeClick = { onEvent(SessionUiEvent.SessionManagementEvent.ResumeClicked) },
                    onStopClick = { onEvent(SessionUiEvent.SessionManagementEvent.StopClicked) },
                    onEnableLocationClick = { onEvent(SessionUiEvent.LocationSettingsEvent.EnableLocationClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
                when (uiState.overlay) {
                    SessionOverlay.StopConfirmation -> {
                        StopConfirmationDialog(
                            onConfirm = { onEvent(SessionUiEvent.SessionManagementEvent.StopConfirmed) },
                            onDismiss = { onEvent(SessionUiEvent.SessionManagementEvent.StopConfirmationDismissed) },
                        )
                    }

                    SessionOverlay.LocationDisabled -> {
                        LocationDisabledDialog(
                            onEnableClick = {
                                shouldResolveLocation = true
                            },
                            onDismiss = {
                                onEvent(SessionUiEvent.LocationSettingsEvent.LocationDisabledDismissed)
                            },
                        )
                    }

                    is SessionOverlay.Error -> Unit
                    null -> Unit
                }
            }
        }
    }
}
