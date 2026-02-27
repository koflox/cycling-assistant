package com.koflox.poisettings.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface PoiSettingsUiNavigator {

    @Composable
    fun PoiSettingsSection(
        onNavigateToPoiSelection: () -> Unit,
        modifier: Modifier,
    )

    @Composable
    fun PoiSelectionScreen(
        onBackClick: () -> Unit,
        modifier: Modifier,
    )
}
