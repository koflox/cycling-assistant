package com.koflox.connections.presentation.listing.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.connections.R
import com.koflox.connections.domain.model.DeviceType
import com.koflox.designsystem.theme.Spacing

@Composable
internal fun DeviceTypeChip(
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
) {
    val label = when (deviceType) {
        DeviceType.POWER_METER -> stringResource(R.string.connections_device_type_power_meter)
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.Tiny),
        )
    }
}
