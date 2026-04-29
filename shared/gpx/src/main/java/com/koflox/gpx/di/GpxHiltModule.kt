package com.koflox.gpx.di

import com.koflox.gpx.GpxMapper
import com.koflox.gpx.GpxMapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object GpxHiltModule {

    @Provides
    @Singleton
    fun provideGpxMapper(): GpxMapper = GpxMapperImpl()
}
