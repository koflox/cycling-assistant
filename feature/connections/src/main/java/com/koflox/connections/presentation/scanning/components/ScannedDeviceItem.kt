package com.koflox.connections.presentation.scanning.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.connections.R
import com.koflox.connections.presentation.scanning.ScannedDeviceUiModel
import com.koflox.designsystem.theme.Spacing

@Composable
internal fun ScannedDeviceItem(
    device: ScannedDeviceUiModel,
    onDeviceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !device.isAlreadyPaired, onClick = onDeviceClick)
            .padding(vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = device.macAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (device.isAlreadyPaired) {
            Text(
                text = stringResource(R.string.connections_scan_already_paired),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
