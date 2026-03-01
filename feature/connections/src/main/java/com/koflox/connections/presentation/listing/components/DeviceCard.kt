package com.koflox.connections.presentation.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.connections.R
import com.koflox.connections.presentation.listing.DeviceListItemUiModel
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.theme.Spacing

@Composable
internal fun DeviceCard(
    device: DeviceListItemUiModel,
    onTestModeClick: () -> Unit,
    onToggleSessionUsage: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.Medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = device.macAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DeviceTypeChip(deviceType = device.deviceType)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.connections_device_session_usage),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = device.isSessionUsageEnabled,
                        onCheckedChange = onToggleSessionUsage,
                        modifier = Modifier.padding(start = Spacing.Small),
                    )
                }
                DebouncedOutlinedButton(onClick = onTestModeClick) {
                    Text(text = stringResource(R.string.connections_device_test_mode))
                }
            }
        }
    }
}
