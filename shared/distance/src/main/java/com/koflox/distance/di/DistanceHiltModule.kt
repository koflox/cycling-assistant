package com.koflox.distance.di

import com.koflox.distance.DefaultDistanceCalculator
import com.koflox.distance.DistanceCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DistanceHiltModule {

    @Provides
    @Singleton
    fun provideDistanceCalculator(): DistanceCalculator = DefaultDistanceCalculator()
}
