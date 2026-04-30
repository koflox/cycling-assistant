package com.koflox.strava.impl.presentation.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.strava.api.navigator.StravaSettingsNavigator
import com.koflox.strava.impl.presentation.settings.StravaSettingsSectionRoute
import javax.inject.Inject

internal class StravaSettingsNavigatorImpl @Inject constructor() : StravaSettingsNavigator {

    @Composable
    override fun StravaSettingsSection(
        onNavigateToConnect: () -> Unit,
        modifier: Modifier,
    ) {
        StravaSettingsSectionRoute(
            onNavigateToConnect = onNavigateToConnect,
            modifier = modifier,
        )
    }
}
