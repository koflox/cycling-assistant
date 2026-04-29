package com.koflox.strava.impl.di

import com.koflox.strava.api.navigator.StravaSettingsNavigator
import com.koflox.strava.api.navigator.StravaShareTabNavigator
import com.koflox.strava.impl.presentation.navigator.StravaSettingsNavigatorImpl
import com.koflox.strava.impl.presentation.navigator.StravaShareTabNavigatorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object StravaPresentationHiltModule {

    @Provides
    @Singleton
    fun provideStravaShareTabNavigator(): StravaShareTabNavigator = StravaShareTabNavigatorImpl()

    @Provides
    @Singleton
    fun provideStravaSettingsNavigator(): StravaSettingsNavigator = StravaSettingsNavigatorImpl()
}
