package com.koflox.nutritionsettings.bridge.impl.di

import com.koflox.nutritionsettings.bridge.impl.navigator.NutritionSettingsUiNavigatorImpl
import com.koflox.nutritionsettings.bridge.navigator.NutritionSettingsUiNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideNutritionSettingsUiNavigator(): NutritionSettingsUiNavigator = NutritionSettingsUiNavigatorImpl()
}
