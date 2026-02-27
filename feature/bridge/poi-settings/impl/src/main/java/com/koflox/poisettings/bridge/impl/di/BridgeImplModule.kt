package com.koflox.poisettings.bridge.impl.di

import com.koflox.poisettings.bridge.impl.navigator.PoiSettingsUiNavigatorImpl
import com.koflox.poisettings.bridge.navigator.PoiSettingsUiNavigator
import org.koin.dsl.module

val poiSettingsBridgeImplModule = module {
    factory<PoiSettingsUiNavigator> {
        PoiSettingsUiNavigatorImpl()
    }
}
