package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.mapper.SessionMapperImpl
import com.koflox.session.data.repository.SessionFlushDecider
import com.koflox.session.data.repository.SessionFlushDeciderImpl
import com.koflox.session.data.repository.SessionRepositoryImpl
import com.koflox.session.data.repository.StatsDisplayRepositoryImpl
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.data.source.local.SessionLocalDataSourceImpl
import com.koflox.session.data.source.local.StatsDisplayLocalDataSource
import com.koflox.session.data.source.local.StatsDisplayPreferencesDataStore
import com.koflox.session.data.source.runtime.SessionRuntimeDataSource
import com.koflox.session.data.source.runtime.SessionRuntimeDataSourceImpl
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.domain.repository.StatsDisplayRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
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
            daoFactory = get(SessionQualifier.DaoFactory),
        )
    }
    single<SessionRuntimeDataSource> {
        SessionRuntimeDataSourceImpl()
    }
    single<StatsDisplayLocalDataSource> {
        StatsDisplayPreferencesDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<SessionFlushDecider> {
        SessionFlushDeciderImpl(
            currentTimeProvider = get(),
        )
    }
    single<SessionRepository> {
        SessionRepositoryImpl(
            localDataSource = get(),
            runtimeDataSource = get(),
            mapper = get(),
            flushDecider = get(),
            currentTimeProvider = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
    single<StatsDisplayRepository> {
        StatsDisplayRepositoryImpl(
            localDataSource = get(),
        )
    }
}

internal val dataModules: List<Module> = listOf(
    dataModule,
    dataSourceModule,
    repoModule,
)
