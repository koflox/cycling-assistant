package com.koflox.error.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.error.mapper.DefaultErrorMessageMapper
import com.koflox.error.mapper.ErrorMessageMapper
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

val errorMapperModule = module {
    single<ErrorMessageMapper> {
        DefaultErrorMessageMapper(
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
        )
    }
}
