package com.koflox.nutritionsettings.bridge.impl.di

import com.koflox.nutritionsettings.bridge.impl.navigator.NutritionSettingsUiNavigatorImpl
import com.koflox.nutritionsettings.bridge.navigator.NutritionSettingsUiNavigator
import org.koin.dsl.module

val nutritionSettingsBridgeImplModule = module {
    factory<NutritionSettingsUiNavigator> {
        NutritionSettingsUiNavigatorImpl()
    }
}
