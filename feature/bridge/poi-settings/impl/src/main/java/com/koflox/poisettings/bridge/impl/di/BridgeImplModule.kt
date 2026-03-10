package com.koflox.poisettings.bridge.impl.di

import com.koflox.poisettings.bridge.impl.navigator.PoiSettingsUiNavigatorImpl
import com.koflox.poisettings.bridge.navigator.PoiSettingsUiNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun providePoiSettingsUiNavigator(): PoiSettingsUiNavigator = PoiSettingsUiNavigatorImpl()
}
