package com.koflox.locale.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.locale.data.repository.LocaleRepositoryImpl
import com.koflox.locale.data.source.LocaleLocalDataSource
import com.koflox.locale.data.source.LocaleRoomDataSource
import com.koflox.locale.domain.repository.LocaleRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

private val dataSourceModule = module {
    single<LocaleLocalDataSource> {
        LocaleRoomDataSource(
            daoFactory = get(LocaleQualifier.DaoFactory),
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
