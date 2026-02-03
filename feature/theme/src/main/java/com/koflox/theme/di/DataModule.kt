package com.koflox.theme.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.theme.data.repository.ThemeRepositoryImpl
import com.koflox.theme.data.source.ThemeDataStore
import com.koflox.theme.data.source.ThemeLocalDataSource
import com.koflox.theme.domain.repository.ThemeRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val dataSourceModule = module {
    single<ThemeLocalDataSource> {
        ThemeDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<ThemeRepository> {
        ThemeRepositoryImpl(localDataSource = get())
    }
}

internal val dataModules = listOf(dataSourceModule, repoModule)
