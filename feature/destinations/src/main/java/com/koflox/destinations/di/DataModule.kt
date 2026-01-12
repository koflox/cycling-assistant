package com.koflox.destinations.di

import android.content.Context
import androidx.room.Room
import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.mapper.DestinationMapperImpl
import com.koflox.destinations.data.repository.DestinationRepositoryImpl
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.asset.PoiAssetDataSourceImpl
import com.koflox.destinations.data.source.local.database.AppDatabase
import com.koflox.destinations.data.source.prefs.PreferencesDataSource
import com.koflox.destinations.data.source.prefs.PreferencesDataSourceImpl
import com.koflox.destinations.domain.repository.DestinationRepository
import kotlinx.coroutines.CoroutineDispatcher
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
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()
    }
    single {
        get<AppDatabase>().destinationDao()
    }
    single<PoiAssetDataSource> {
        PoiAssetDataSourceImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            context = androidContext(),
        )
    }
    single<PreferencesDataSource> {
        PreferencesDataSourceImpl(
            dispatcherIo = get(DispatchersQualifier.Io),
            prefs = androidContext().getSharedPreferences("destinations_prefs", Context.MODE_PRIVATE),
        )
    }
}

private val repoModule = module {
    single<DestinationRepository> {
        DestinationRepositoryImpl(
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            dao = get(),
            poiAssetDataSource = get(),
            locationDataSource = get(),
            preferencesDataSource = get(),
            mapper = get(),
        )
    }
}

internal val dataModules: List<Module> = listOf(
    dataModule,
    dataSourceModule,
    repoModule,
)
