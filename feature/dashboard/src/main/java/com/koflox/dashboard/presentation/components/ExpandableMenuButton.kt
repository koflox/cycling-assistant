package com.koflox.dashboard.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.koflox.dashboard.R
import com.koflox.designsystem.component.FloatingMenuButton

@Composable
internal fun ExpandableMenuButton(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSessionsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FloatingMenuButton(
            icon = Icons.Default.Menu,
            contentDescription = stringResource(R.string.menu_button),
            onClick = onToggleExpand,
        )
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FloatingMenuButton(
                    icon = Icons.Default.Route,
                    contentDescription = stringResource(R.string.menu_sessions),
                    onClick = onSessionsClick,
                )
                FloatingMenuButton(
                    icon = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.menu_settings),
                    onClick = onSettingsClick,
                )
            }
        }
    }
}
