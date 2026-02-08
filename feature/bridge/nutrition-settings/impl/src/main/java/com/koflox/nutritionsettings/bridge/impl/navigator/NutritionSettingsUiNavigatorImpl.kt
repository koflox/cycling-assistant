package com.koflox.nutritionsettings.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.nutrition.presentation.settings.NutritionSettingsSectionRoute
import com.koflox.nutritionsettings.bridge.navigator.NutritionSettingsUiNavigator

internal class NutritionSettingsUiNavigatorImpl : NutritionSettingsUiNavigator {

    @Composable
    override fun NutritionSettingsSection(modifier: Modifier) {
        NutritionSettingsSectionRoute(modifier = modifier)
    }
}
