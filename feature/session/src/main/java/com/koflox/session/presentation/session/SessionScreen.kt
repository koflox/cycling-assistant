package com.koflox.session.presentation.session

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
            viewModel.onEvent(SessionUiEvent.ErrorDismissed)
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
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            SessionUiState.Idle -> Unit
            is SessionUiState.Active -> {
                SessionControlsOverlay(
                    state = uiState,
                    onPauseClick = { onEvent(SessionUiEvent.PauseClicked) },
                    onResumeClick = { onEvent(SessionUiEvent.ResumeClicked) },
                    onStopClick = { onEvent(SessionUiEvent.StopClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
                if (uiState.overlay is SessionOverlay.StopConfirmation) {
                    StopConfirmationDialog(
                        onConfirm = { onEvent(SessionUiEvent.StopConfirmed) },
                        onDismiss = { onEvent(SessionUiEvent.StopConfirmationDismissed) },
                    )
                }
            }
        }
    }
}
