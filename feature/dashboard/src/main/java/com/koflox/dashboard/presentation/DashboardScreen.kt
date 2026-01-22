package com.koflox.dashboard.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.koflox.dashboard.presentation.components.ExpandableMenuButton
import com.koflox.designsystem.theme.Spacing
import com.koflox.destinations.presentation.destinations.DestinationsScreen

@Composable
internal fun DashboardScreen(
    onNavigateToSessionsList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize()) {
        DestinationsScreen(
            onNavigateToSessionCompletion = onNavigateToSessionCompletion,
            modifier = Modifier.fillMaxSize(),
        )
        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { isMenuExpanded = false },
            )
        }
        ExpandableMenuButton(
            isExpanded = isMenuExpanded,
            onToggleExpand = { isMenuExpanded = !isMenuExpanded },
            onSessionsClick = {
                isMenuExpanded = false
                onNavigateToSessionsList()
            },
            onSettingsClick = {
                isMenuExpanded = false
                onNavigateToSettings()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(Spacing.Large),
        )
    }
}
