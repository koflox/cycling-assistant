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
fun SessionScreen(
    onNavigateToCompletion: (sessionId: String) -> Unit,
    viewModel: SessionViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SessionUiEvent.ErrorDismissed)
        }
    }
    LaunchedEffect(state.completedSessionId) {
        state.completedSessionId?.let { sessionId ->
            onNavigateToCompletion(sessionId)
            viewModel.onEvent(SessionUiEvent.CompletedSessionNavigated)
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (state.isActive) {
            SessionControlsOverlay(
                state = state,
                onPauseClick = { viewModel.onEvent(SessionUiEvent.PauseClicked) },
                onResumeClick = { viewModel.onEvent(SessionUiEvent.ResumeClicked) },
                onStopClick = { viewModel.onEvent(SessionUiEvent.StopClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            )
        }
    }
    if (state.showStopConfirmationDialog) {
        StopConfirmationDialog(
            onConfirm = { viewModel.onEvent(SessionUiEvent.StopConfirmed) },
            onDismiss = { viewModel.onEvent(SessionUiEvent.StopConfirmationDismissed) },
        )
    }
}
