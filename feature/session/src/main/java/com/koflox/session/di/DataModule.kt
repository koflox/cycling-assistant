package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.mapper.SessionMapperImpl
import com.koflox.session.data.repository.SessionRepositoryImpl
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

internal val dataModule = module {
    single<SessionMapper> {
        SessionMapperImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }

    single<SessionRepository> {
        SessionRepositoryImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            sessionDao = get(),
            mapper = get(),
        )
    }
}
