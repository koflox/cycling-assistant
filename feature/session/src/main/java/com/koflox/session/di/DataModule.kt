package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.mapper.SessionMapperImpl
import com.koflox.session.data.repository.SessionRepositoryImpl
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.data.source.local.SessionLocalDataSourceImpl
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import org.koin.dsl.module

private val dataModule = module {
    single<SessionMapper> {
        SessionMapperImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
}

private val dataSourceModule = module {
    single<SessionLocalDataSource> {
        SessionLocalDataSourceImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            dao = get(),
        )
    }
}

private val repoModule = module {
    single<SessionRepository> {
        SessionRepositoryImpl(
            localDataSource = get(),
            mapper = get(),
        )
    }
}

internal val dataModules: List<Module> = listOf(
    dataModule,
    dataSourceModule,
    repoModule,
)
