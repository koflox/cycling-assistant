package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.mapper.DestinationMapperImpl
import com.koflox.destinations.data.repository.DestinationRepositoryImpl
import com.koflox.destinations.data.source.asset.DestinationFileResolver
import com.koflox.destinations.data.source.asset.DestinationFileResolverImpl
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.asset.PoiAssetDataSourceImpl
import com.koflox.destinations.data.source.local.DestinationFilesLocalDataSource
import com.koflox.destinations.data.source.local.DestinationFilesLocalDataSourceImpl
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.data.source.local.PoiLocalDataSourceImpl
import com.koflox.destinations.domain.repository.DestinationRepository
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
    // DestinationDao is provided by :feature:database module
    single<PoiLocalDataSource> {
        PoiLocalDataSourceImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            dao = get(),
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
}

private val repoModule = module {
    single<DestinationRepository> {
        DestinationRepositoryImpl(
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            poiLocalDataSource = get(),
            poiAssetDataSource = get(),
            locationDataSource = get(),
            destinationFilesLocalDataSource = get(),
            destinationFileResolver = get(),
            mapper = get(),
            mutex = Mutex(),
        )
    }
}

internal val dataModules: List<Module> = listOf(
    dataModule,
    dataSourceModule,
    repoModule,
)
