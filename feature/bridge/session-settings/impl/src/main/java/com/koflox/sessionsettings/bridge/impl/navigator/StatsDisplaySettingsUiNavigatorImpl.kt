package com.koflox.sessionsettings.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.session.presentation.statsdisplay.StatsDisplayConfigRoute
import com.koflox.session.presentation.statsdisplay.StatsDisplaySection
import com.koflox.session.presentation.statsdisplay.StatsDisplaySettingsSectionRoute
import com.koflox.sessionsettings.bridge.navigator.StatsDisplaySettingsUiNavigator

internal class StatsDisplaySettingsUiNavigatorImpl : StatsDisplaySettingsUiNavigator {

    @Composable
    override fun StatsDisplaySettingsSection(
        onNavigateToStatsConfig: () -> Unit,
        modifier: Modifier,
    ) {
        StatsDisplaySettingsSectionRoute(
            onNavigateToStatsConfig = onNavigateToStatsConfig,
            modifier = modifier,
        )
    }

    @Composable
    override fun StatsDisplayConfigScreen(
        onBackClick: () -> Unit,
        initialSection: String?,
        modifier: Modifier,
    ) {
        val section = initialSection?.let { arg ->
            StatsDisplaySection.entries.find { it.name == arg }
        }
        StatsDisplayConfigRoute(
            onBackClick = onBackClick,
            initialSection = section,
            modifier = modifier,
        )
    }
}
