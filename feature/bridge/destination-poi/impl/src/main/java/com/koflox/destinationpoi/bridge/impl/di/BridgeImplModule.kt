package com.koflox.destinationpoi.bridge.impl.di

import com.koflox.destinationpoi.bridge.impl.navigator.PoiUiNavigatorImpl
import com.koflox.destinationpoi.bridge.navigator.PoiUiNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun providePoiUiNavigator(): PoiUiNavigator = PoiUiNavigatorImpl()
}
