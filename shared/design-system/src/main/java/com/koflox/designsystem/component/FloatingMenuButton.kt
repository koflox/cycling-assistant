package com.koflox.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.theme.CornerRadius
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Spacing

@Composable
fun FloatingMenuButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconPadding: Dp = Spacing.Medium,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = Elevation.Prominent,
        tonalElevation = Elevation.Subtle,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(iconPadding),
        )
    }
}
