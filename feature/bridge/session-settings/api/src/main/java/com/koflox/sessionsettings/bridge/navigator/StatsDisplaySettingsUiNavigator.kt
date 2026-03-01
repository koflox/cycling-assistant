package com.koflox.sessionsettings.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface StatsDisplaySettingsUiNavigator {

    @Composable
    fun StatsDisplaySettingsSection(
        onNavigateToStatsConfig: () -> Unit,
        modifier: Modifier,
    )

    @Composable
    fun StatsDisplayConfigScreen(
        onBackClick: () -> Unit,
        initialSection: String?,
        modifier: Modifier,
    )
}
