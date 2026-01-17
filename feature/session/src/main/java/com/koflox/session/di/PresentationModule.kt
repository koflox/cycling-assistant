package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.di.ClassNameQualifier
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.presentation.error.SessionErrorMessageMapper
import com.koflox.session.presentation.session.SessionViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal sealed class PresentationModuleQualifier : ClassNameQualifier() {
    object SessionErrorMessageMapper : PresentationModuleQualifier()
}

internal val presentationModule = module {
    single<ErrorMessageMapper>(PresentationModuleQualifier.SessionErrorMessageMapper) {
        SessionErrorMessageMapper(
            context = androidContext(),
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            defaultMapper = get(),
        )
    }
    viewModelOf(::SessionViewModel)
}
