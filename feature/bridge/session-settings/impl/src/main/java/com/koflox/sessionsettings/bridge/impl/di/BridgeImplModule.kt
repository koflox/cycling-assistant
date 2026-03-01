package com.koflox.sessionsettings.bridge.impl.di

import com.koflox.sessionsettings.bridge.impl.navigator.StatsDisplaySettingsUiNavigatorImpl
import com.koflox.sessionsettings.bridge.navigator.StatsDisplaySettingsUiNavigator
import org.koin.dsl.module

val sessionSettingsBridgeImplModule = module {
    factory<StatsDisplaySettingsUiNavigator> {
        StatsDisplaySettingsUiNavigatorImpl()
    }
}
