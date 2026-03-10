package com.koflox.id.di

import com.koflox.id.IdGenerator
import com.koflox.id.UuidIdGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object IdHiltModule {

    @Provides
    @Singleton
    fun provideIdGenerator(): IdGenerator = UuidIdGenerator()
}
