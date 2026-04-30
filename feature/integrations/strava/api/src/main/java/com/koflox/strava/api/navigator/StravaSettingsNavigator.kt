package com.koflox.strava.api.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface StravaSettingsNavigator {

    @Composable
    fun StravaSettingsSection(
        onNavigateToConnect: () -> Unit,
        modifier: Modifier,
    )
}
