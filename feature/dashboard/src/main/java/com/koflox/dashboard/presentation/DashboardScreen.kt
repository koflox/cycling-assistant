package com.koflox.dashboard.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koflox.dashboard.presentation.components.SessionsListButton
import com.koflox.destinations.presentation.destinations.DestinationsScreen

@Composable
internal fun DashboardScreen(
    onNavigateToSessionsList: () -> Unit,
    onNavigateToSessionCompletion: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        DestinationsScreen(
            onNavigateToSessionCompletion = onNavigateToSessionCompletion,
            modifier = Modifier.fillMaxSize(),
        )
        SessionsListButton(
            onClick = onNavigateToSessionsList,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp),
        )
    }
}
