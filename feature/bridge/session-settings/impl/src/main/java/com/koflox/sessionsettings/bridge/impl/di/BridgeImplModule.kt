package com.koflox.sessionsettings.bridge.impl.di

import com.koflox.sessionsettings.bridge.impl.navigator.StatsDisplaySettingsUiNavigatorImpl
import com.koflox.sessionsettings.bridge.navigator.StatsDisplaySettingsUiNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideStatsDisplaySettingsUiNavigator(): StatsDisplaySettingsUiNavigator = StatsDisplaySettingsUiNavigatorImpl()
}
