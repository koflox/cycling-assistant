package com.koflox.poisettings.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.poi.presentation.settings.PoiSettingsSectionRoute
import com.koflox.poisettings.bridge.navigator.PoiSettingsUiNavigator

internal class PoiSettingsUiNavigatorImpl : PoiSettingsUiNavigator {

    @Composable
    override fun PoiSettingsSection(
        onNavigateToPoiSelection: () -> Unit,
        modifier: Modifier,
    ) {
        PoiSettingsSectionRoute(
            onNavigateToPoiSelection = onNavigateToPoiSelection,
            modifier = modifier,
        )
    }
}
