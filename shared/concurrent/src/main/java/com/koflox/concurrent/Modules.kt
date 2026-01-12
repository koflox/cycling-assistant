package com.koflox.concurrent

import com.koflox.di.ClassNameQualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

sealed class DispatchersQualifier : ClassNameQualifier(), Qualifier {
    object Io : DispatchersQualifier()
    object Main : DispatchersQualifier()
    object Default : DispatchersQualifier()
    object Unconfined : DispatchersQualifier()
}

val concurrentModule = module {
    single<CoroutineDispatcher>(DispatchersQualifier.Io) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(DispatchersQualifier.Main) {
        Dispatchers.Main
    }
    single<CoroutineDispatcher>(DispatchersQualifier.Default) {
        Dispatchers.Default
    }
    single<CoroutineDispatcher>(DispatchersQualifier.Unconfined) {
        Dispatchers.Unconfined
    }
}
