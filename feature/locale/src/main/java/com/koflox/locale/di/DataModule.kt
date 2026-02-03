package com.koflox.locale.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.locale.data.repository.LocaleRepositoryImpl
import com.koflox.locale.data.source.LocaleDataStore
import com.koflox.locale.data.source.LocaleLocalDataSource
import com.koflox.locale.domain.repository.LocaleRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val dataSourceModule = module {
    single<LocaleLocalDataSource> {
        LocaleDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<LocaleRepository> {
        LocaleRepositoryImpl(localDataSource = get())
    }
}

internal val dataModules = listOf(dataSourceModule, repoModule)
