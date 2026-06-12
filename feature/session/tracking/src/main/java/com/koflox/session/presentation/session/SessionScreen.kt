package com.koflox.session.presentation.session

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.koflox.ble.permission.BlePermissionChecker
import com.koflox.designsystem.text.resolve
import com.koflox.location.settings.LocationSettingsHandler
import com.koflox.session.presentation.dialog.LocationDisabledDialog
import com.koflox.session.presentation.dialog.StopConfirmationDialog
import com.koflox.session.presentation.session.components.SessionControlsOverlay
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface SessionBlePermissionEntryPoint {
    fun blePermissionChecker(): BlePermissionChecker
}

@Composable
fun SessionScreenRoute(
    onNavigateToCompletion: (sessionId: String) -> Unit,
    onNavigateToConnections: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SessionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                is SessionNavigation.ToCompletion -> onNavigateToCompletion(event.sessionId)
                SessionNavigation.ToConnections -> onNavigateToConnections()
            }
        }
    }
    LaunchedEffect(uiState) {
        val active = uiState as? SessionUiState.Active ?: return@LaunchedEffect
        if (active.overlay is SessionOverlay.Error) {
            Toast.makeText(context, active.overlay.message.resolve(context), Toast.LENGTH_SHORT).show()
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
    val onRequestBlePermission = rememberBlePermissionRequest()
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
                    onDeviceStripClick = { onEvent(SessionUiEvent.DeviceEvent.StripClicked) },
                    onRequestBlePermission = onRequestBlePermission,
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberBlePermissionRequest(): () -> Unit {
    val context = LocalContext.current
    val permissions = remember {
        EntryPointAccessors.fromApplication(context, SessionBlePermissionEntryPoint::class.java)
            .blePermissionChecker()
            .requiredPermissions()
    }
    val permissionsState = rememberMultiplePermissionsState(permissions)
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    return {
        // While a rationale can still be shown (or the permission was never requested) launch the
        // runtime request; once it is permanently denied the system dialog no longer appears, so
        // fall back to the app's settings page.
        if (!hasRequested || permissionsState.shouldShowRationale) {
            hasRequested = true
            permissionsState.launchMultiplePermissionRequest()
        } else {
            context.openAppSettings()
        }
    }
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}
