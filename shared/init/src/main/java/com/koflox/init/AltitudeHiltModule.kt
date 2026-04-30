package com.koflox.init

import com.koflox.altitude.AltitudeCalculator
import com.koflox.altitude.DefaultAltitudeCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AltitudeHiltModule {

    @Provides
    @Singleton
    fun provideAltitudeCalculator(): AltitudeCalculator = DefaultAltitudeCalculator()
}
