package com.koflox.map.di

import android.app.Application
import com.koflox.map.intent.GoogleMapsIntentHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MapHiltModule {

    @Provides
    @Singleton
    fun provideGoogleMapsIntentHelper(application: Application): GoogleMapsIntentHelper =
        GoogleMapsIntentHelper(application = application)
}
