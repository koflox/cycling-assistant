package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.mapper.DestinationMapperImpl
import com.koflox.destinations.data.repository.DestinationsRepositoryImpl
import com.koflox.destinations.data.repository.RidePreferencesRepositoryImpl
import com.koflox.destinations.data.repository.UserLocationRepositoryImpl
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
import com.koflox.destinations.domain.repository.DestinationsRepository
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import com.koflox.destinations.domain.repository.UserLocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private val dataModule = module {
    single<DestinationMapper> {
        DestinationMapperImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
}

private val dataSourceModule = module {
    single<PoiLocalDataSource> {
        PoiLocalDataSourceImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            daoFactory = get(DestinationsQualifier.DaoFactory),
        )
    }
    single<PoiAssetDataSource> {
        PoiAssetDataSourceImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            context = androidContext(),
        )
    }
    single<DestinationFilesLocalDataSource> {
        DestinationFilesLocalDataSourceImpl(
            dispatcherIo = get(DispatchersQualifier.Io),
            context = androidContext(),
        )
    }
    single<DestinationFileResolver> {
        DestinationFileResolverImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            context = androidContext(),
            distanceCalculator = get(),
        )
    }
    single<RidingModeLocalDataSource> {
        RidingModeLocalDataSourceImpl(
            context = androidContext(),
            dispatcherIo = get(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<DestinationsRepository> {
        DestinationsRepositoryImpl(
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            poiLocalDataSource = get(),
            poiAssetDataSource = get(),
            destinationFilesLocalDataSource = get(),
            destinationFileResolver = get(),
            mapper = get(),
            mutex = Mutex(),
        )
    }
    single<UserLocationRepository> {
        UserLocationRepositoryImpl(
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            locationDataSource = get(),
        )
    }
    single<RidePreferencesRepository> {
        RidePreferencesRepositoryImpl(
            localDataSource = get(),
        )
    }
}

internal val dataModules: List<Module> = listOf(
    dataModule,
    dataSourceModule,
    repoModule,
)
