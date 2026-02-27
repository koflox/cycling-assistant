package com.koflox.poi.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.poi.data.repository.PoiRepositoryImpl
import com.koflox.poi.data.source.PoiLocalDataSource
import com.koflox.poi.data.source.PoiPreferencesDataStore
import com.koflox.poi.domain.repository.PoiRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val dataSourceModule = module {
    single<PoiLocalDataSource> {
        PoiPreferencesDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<PoiRepository> {
        PoiRepositoryImpl(localDataSource = get())
    }
}

internal val dataModules = listOf(dataSourceModule, repoModule)
