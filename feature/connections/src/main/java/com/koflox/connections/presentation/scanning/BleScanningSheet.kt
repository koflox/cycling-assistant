package com.koflox.connections.presentation.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.koflox.connections.R
import com.koflox.connections.presentation.permission.BlePermissionHandler
import com.koflox.connections.presentation.scanning.components.ScannedDeviceItem
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BleScanningSheetRoute(
    onDismiss: () -> Unit,
    viewModel: BleScanningViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDismissed by viewModel.isDismissed.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(isDismissed) {
        if (isDismissed) onDismiss()
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        BleScanningSheetContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun BleScanningSheetContent(
    uiState: BleScanningUiState,
    onEvent: (BleScanningUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Large),
    ) {
        Text(
            text = stringResource(R.string.connections_scan_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.connections_scan_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        BleScanningStateBody(uiState = uiState, onEvent = onEvent)
        Spacer(modifier = Modifier.height(Spacing.Large))
    }
}

@Composable
private fun BleScanningStateBody(
    uiState: BleScanningUiState,
    onEvent: (BleScanningUiEvent) -> Unit,
) {
    when (uiState) {
        is BleScanningUiState.PermissionRequired -> {
            BlePermissionHandler(
                onPermissionGranted = { onEvent(BleScanningUiEvent.PermissionGranted) },
                onPermissionDenied = { onEvent(BleScanningUiEvent.PermissionDenied) },
            )
        }
        is BleScanningUiState.Scanning -> {
            if (uiState.devices.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(Spacing.Small))
                    Text(
                        text = stringResource(R.string.connections_scan_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn {
                    items(items = uiState.devices, key = { it.macAddress }) { device ->
                        ScannedDeviceItem(
                            device = device,
                            onDeviceClick = {
                                if (!device.isAlreadyPaired) {
                                    onEvent(BleScanningUiEvent.DeviceSelected(device.macAddress, device.name))
                                }
                            },
                        )
                    }
                }
            }
        }
        is BleScanningUiState.Error -> {
            val context = LocalContext.current
            Text(
                text = uiState.message.resolve(context),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
