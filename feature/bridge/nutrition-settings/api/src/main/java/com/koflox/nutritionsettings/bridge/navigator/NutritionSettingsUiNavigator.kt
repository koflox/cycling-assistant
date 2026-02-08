package com.koflox.nutritionsettings.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface NutritionSettingsUiNavigator {
    @Composable
    fun NutritionSettingsSection(modifier: Modifier)
}
