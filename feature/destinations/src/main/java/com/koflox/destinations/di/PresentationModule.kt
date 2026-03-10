package com.koflox.destinations.di

import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinations.presentation.mapper.DestinationUiMapperImpl
import com.koflox.di.DefaultDispatcher
import com.koflox.distance.DistanceCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PresentationModule {

    @Provides
    @Singleton
    fun provideDestinationUiMapper(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        distanceCalculator: DistanceCalculator,
    ): DestinationUiMapper = DestinationUiMapperImpl(
        dispatcherDefault = dispatcherDefault,
        distanceCalculator = distanceCalculator,
    )
}
