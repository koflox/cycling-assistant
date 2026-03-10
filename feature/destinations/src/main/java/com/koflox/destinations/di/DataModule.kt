package com.koflox.destinations.di

import android.content.Context
import com.koflox.concurrent.ConcurrentFactory
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.mapper.DestinationMapperImpl
import com.koflox.destinations.data.repository.DestinationsRepositoryImpl
import com.koflox.destinations.data.repository.RidePreferencesRepositoryImpl
import com.koflox.destinations.data.source.asset.DestinationFileResolver
import com.koflox.destinations.data.source.asset.DestinationFileResolverImpl
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.asset.PoiAssetDataSourceImpl
import com.koflox.destinations.data.source.local.DestinationFilesLocalDataSource
import com.koflox.destinations.data.source.local.DestinationFilesLocalDataSourceImpl
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.data.source.local.PoiLocalDataSourceImpl
import com.koflox.destinations.data.source.local.RidingModeLocalDataSource
import com.koflox.destinations.data.source.local.RidingModeLocalDataSourceImpl
import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.domain.repository.DestinationsRepository
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import com.koflox.di.DefaultDispatcher
import com.koflox.di.DestinationFilesMutex
import com.koflox.di.DestinationsDaoFactory
import com.koflox.di.IoDispatcher
import com.koflox.distance.DistanceCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Provides
    @Singleton
    fun provideDestinationMapper(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
    ): DestinationMapper = DestinationMapperImpl(
        dispatcherDefault = dispatcherDefault,
    )

    @Provides
    @Singleton
    @DestinationFilesMutex
    fun provideDestinationFilesMutex(): Mutex = Mutex()

    @Provides
    @Singleton
    fun providePoiLocalDataSource(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        @DestinationsDaoFactory daoFactory: ConcurrentFactory<DestinationDao>,
    ): PoiLocalDataSource = PoiLocalDataSourceImpl(
        dispatcherIo = dispatcherIo,
        daoFactory = daoFactory,
    )

    @Provides
    @Singleton
    fun providePoiAssetDataSource(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        @ApplicationContext context: Context,
    ): PoiAssetDataSource = PoiAssetDataSourceImpl(
        dispatcherIo = dispatcherIo,
        context = context,
    )

    @Provides
    @Singleton
    fun provideDestinationFilesLocalDataSource(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        @ApplicationContext context: Context,
        @DestinationFilesMutex mutex: Mutex,
    ): DestinationFilesLocalDataSource = DestinationFilesLocalDataSourceImpl(
        dispatcherIo = dispatcherIo,
        context = context,
        mutex = mutex,
    )

    @Provides
    @Singleton
    fun provideDestinationFileResolver(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        @ApplicationContext context: Context,
        distanceCalculator: DistanceCalculator,
    ): DestinationFileResolver = DestinationFileResolverImpl(
        dispatcherIo = dispatcherIo,
        context = context,
        distanceCalculator = distanceCalculator,
    )

    @Provides
    @Singleton
    fun provideRidingModeLocalDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): RidingModeLocalDataSource = RidingModeLocalDataSourceImpl(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideDestinationsRepository(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        poiLocalDataSource: PoiLocalDataSource,
        poiAssetDataSource: PoiAssetDataSource,
        destinationFilesLocalDataSource: DestinationFilesLocalDataSource,
        destinationFileResolver: DestinationFileResolver,
        mapper: DestinationMapper,
    ): DestinationsRepository = DestinationsRepositoryImpl(
        dispatcherDefault = dispatcherDefault,
        poiLocalDataSource = poiLocalDataSource,
        poiAssetDataSource = poiAssetDataSource,
        destinationFilesLocalDataSource = destinationFilesLocalDataSource,
        destinationFileResolver = destinationFileResolver,
        mapper = mapper,
        mutex = Mutex(),
    )

    @Provides
    @Singleton
    fun provideRidePreferencesRepository(
        localDataSource: RidingModeLocalDataSource,
    ): RidePreferencesRepository = RidePreferencesRepositoryImpl(
        localDataSource = localDataSource,
    )
}
