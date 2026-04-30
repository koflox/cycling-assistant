package com.koflox.session.presentation.dialog

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.session.domain.usecase.CreateSessionParams
import com.koflox.session.presentation.permission.NotificationPermissionHandler
import com.koflox.session.presentation.session.SessionViewModel

/**
 * Handles the free roam session start flow: requests notification permission,
 * then starts a free roam session via [SessionViewModel].
 *
 * Mirrors the permission + session start flow in [DestinationOptionsRoute]
 * for destination-based sessions.
 */
@Composable
fun FreeRoamSessionRoute() {
    val viewModel: SessionViewModel = hiltViewModel()
    NotificationPermissionHandler { _ ->
        viewModel.startSession(CreateSessionParams.FreeRoam)
    }
}
