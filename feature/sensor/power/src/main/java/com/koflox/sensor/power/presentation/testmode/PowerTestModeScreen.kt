package com.koflox.sensor.power.presentation.testmode

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.LocalizedAlertDialog
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Grid
import com.koflox.designsystem.theme.Spacing
import com.koflox.sensor.power.R
import com.koflox.sensor.power.presentation.testmode.components.CadenceDisplay
import com.koflox.sensor.power.presentation.testmode.components.PowerChart
import com.koflox.sensor.power.presentation.testmode.components.PowerGauge
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun PowerTestModeRoute(
    onBackClick: () -> Unit,
    viewModel: PowerTestModeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                is PowerTestModeNavigation.Back -> onBackClick()
            }
        }
    }
    PowerTestModeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PowerTestModeContent(
    uiState: PowerTestModeUiState,
    onEvent: (PowerTestModeUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.power_test_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.power_test_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.Medium),
        ) {
            PowerTestModeStateBody(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.Center),
            )
            PowerTestModeOverlays(
                overlay = uiState.overlay,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun PowerTestModeStateBody(
    uiState: PowerTestModeUiState,
    onEvent: (PowerTestModeUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PowerTestModeUiState.Connecting -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(Spacing.Medium))
                Text(
                    text = stringResource(R.string.power_test_connecting),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(Spacing.Small))
                Text(
                    text = stringResource(R.string.power_test_connecting_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is PowerTestModeUiState.Connected -> {
            ConnectedContent(state = uiState, onEvent = onEvent)
        }
        is PowerTestModeUiState.Disconnected -> {
            val context = LocalContext.current
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = uiState.message.resolve(context),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(Spacing.Medium))
                DebouncedButton(onClick = { onEvent(PowerTestModeUiEvent.Reconnect) }) {
                    Text(text = stringResource(R.string.power_test_reconnect))
                }
            }
        }
        is PowerTestModeUiState.Error -> {
            val context = LocalContext.current
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = uiState.message.resolve(context),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(Spacing.Medium))
                DebouncedButton(onClick = { onEvent(PowerTestModeUiEvent.Reconnect) }) {
                    Text(text = stringResource(R.string.power_test_reconnect))
                }
            }
        }
    }
}

@Composable
private fun ConnectedContent(
    state: PowerTestModeUiState.Connected,
    onEvent: (PowerTestModeUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PowerGauge(
            powerWatts = state.currentPowerWatts,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        CadenceDisplay(
            cadenceRpm = state.currentCadenceRpm,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        SensorStatsGrid(
            stats = state.sensorStats,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        PowerChart(
            readings = state.recentReadings,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        DebouncedButton(
            onClick = { onEvent(PowerTestModeUiEvent.Disconnect) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.power_test_back))
        }
    }
}

@Composable
private fun SensorStatsGrid(
    stats: List<PowerTestStatItem>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val chunked = stats.chunked(Grid.StatsPerRow)
    Column(modifier = modifier) {
        chunked.forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                rowStats.forEach { stat ->
                    StatItem(
                        label = stat.label.resolve(context),
                        value = stat.value,
                        unit = stat.unit.resolve(context),
                    )
                }
            }
            if (rowStats != chunked.last()) {
                Spacer(modifier = Modifier.height(Spacing.Small))
            }
        }
    }
}

@Composable
private fun PowerTestModeOverlays(
    overlay: PowerTestModeOverlay?,
    onEvent: (PowerTestModeUiEvent) -> Unit,
) {
    val context = LocalContext.current
    when (overlay) {
        is PowerTestModeOverlay.BluetoothDisabled -> {
            LocalizedAlertDialog(
                onDismissRequest = { onEvent(PowerTestModeUiEvent.BluetoothDisabledDismissed) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(PowerTestModeUiEvent.BluetoothDisabledDismissed)
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        },
                    ) {
                        Text(text = stringResource(R.string.power_test_bluetooth_disabled_enable))
                    }
                },
                title = { Text(text = stringResource(R.string.power_test_bluetooth_disabled_title)) },
                text = { Text(text = stringResource(R.string.power_test_bluetooth_disabled_message)) },
            )
        }
        null -> Unit
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
