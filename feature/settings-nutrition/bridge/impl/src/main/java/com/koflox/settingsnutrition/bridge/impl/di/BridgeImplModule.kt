package com.koflox.settingsnutrition.bridge.impl.di

import com.koflox.settingsnutrition.bridge.impl.navigator.NutritionSettingsUiNavigatorImpl
import com.koflox.settingsnutrition.bridge.navigator.NutritionSettingsUiNavigator
import org.koin.dsl.module

val settingsNutritionBridgeImplModule = module {
    factory<NutritionSettingsUiNavigator> {
        NutritionSettingsUiNavigatorImpl()
    }
}
