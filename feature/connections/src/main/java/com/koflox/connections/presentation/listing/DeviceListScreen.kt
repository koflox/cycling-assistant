package com.koflox.connections.presentation.listing

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.connections.R
import com.koflox.connections.presentation.listing.components.DeleteConfirmationDialog
import com.koflox.connections.presentation.listing.components.DeviceCard
import com.koflox.designsystem.component.LocalizedAlertDialog
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing

@Composable
internal fun DeviceListRoute(
    onBackClick: () -> Unit,
    onNavigateToTestMode: (macAddress: String) -> Unit,
    onNavigateToScanning: () -> Unit,
    viewModel: DeviceListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                is DeviceListNavigation.ToTestMode -> onNavigateToTestMode(event.macAddress)
                is DeviceListNavigation.ToScanning -> onNavigateToScanning()
            }
        }
    }
    DeviceListContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceListContent(
    uiState: DeviceListUiState,
    onEvent: (DeviceListUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.connections_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.connections_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(DeviceListUiEvent.AddDeviceClicked) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.connections_add_device),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (uiState) {
                is DeviceListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DeviceListUiState.Empty -> {
                    Text(
                        text = stringResource(R.string.connections_empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is DeviceListUiState.Content -> {
                    DeviceListBody(
                        devices = uiState.devices,
                        onEvent = onEvent,
                    )
                }
            }
            DeviceListOverlays(
                overlay = uiState.overlay,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceListBody(
    devices: List<DeviceListItemUiModel>,
    onEvent: (DeviceListUiEvent) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = devices, key = { it.id }) { device ->
            val dismissState = rememberSwipeToDismissBoxState()
            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                    onEvent(DeviceListUiEvent.DeleteDeviceRequested(device.id, device.name))
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                }
            }
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {},
                enableDismissFromStartToEnd = false,
            ) {
                DeviceCard(
                    device = device,
                    onTestModeClick = { onEvent(DeviceListUiEvent.TestModeClicked(device.macAddress)) },
                    onToggleSessionUsage = { isEnabled ->
                        onEvent(DeviceListUiEvent.ToggleSessionUsage(device.id, isEnabled))
                    },
                    modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                )
            }
        }
    }
}

@Composable
private fun DeviceListOverlays(
    overlay: DeviceListOverlay?,
    onEvent: (DeviceListUiEvent) -> Unit,
) {
    val context = LocalContext.current
    when (overlay) {
        is DeviceListOverlay.Error -> {
            LocalizedAlertDialog(
                onDismissRequest = { onEvent(DeviceListUiEvent.ErrorDismissed) },
                confirmButton = {
                    TextButton(onClick = { onEvent(DeviceListUiEvent.ErrorDismissed) }) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                },
                title = { Text(text = overlay.message.resolve(context)) },
            )
        }
        is DeviceListOverlay.BluetoothDisabled -> {
            LocalizedAlertDialog(
                onDismissRequest = { onEvent(DeviceListUiEvent.BluetoothDisabledDismissed) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(DeviceListUiEvent.BluetoothDisabledDismissed)
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        },
                    ) {
                        Text(text = stringResource(R.string.connections_bluetooth_disabled_enable))
                    }
                },
                title = { Text(text = stringResource(R.string.connections_bluetooth_disabled_title)) },
                text = { Text(text = stringResource(R.string.connections_bluetooth_disabled_message)) },
            )
        }
        is DeviceListOverlay.DeleteConfirmation -> {
            DeleteConfirmationDialog(
                deviceName = overlay.deviceName,
                onConfirm = { onEvent(DeviceListUiEvent.DeleteDeviceConfirmed(overlay.deviceId)) },
                onDismiss = { onEvent(DeviceListUiEvent.DeleteDeviceDismissed) },
            )
        }
        null -> Unit
    }
}
