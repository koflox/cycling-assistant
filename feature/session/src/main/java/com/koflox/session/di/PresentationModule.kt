package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.di.ClassNameQualifier
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.presentation.completion.SessionCompletionViewModel
import com.koflox.session.presentation.error.SessionErrorMessageMapper
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.mapper.SessionUiMapperImpl
import com.koflox.session.presentation.session.SessionViewModel
import com.koflox.session.presentation.sessionslist.SessionsListUiMapper
import com.koflox.session.presentation.sessionslist.SessionsListUiMapperImpl
import com.koflox.session.presentation.sessionslist.SessionsListViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
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
    single<SessionUiMapper> {
        SessionUiMapperImpl()
    }
    viewModel {
        SessionViewModel(
            createSessionUseCase = get(),
            updateSessionStatusUseCase = get(),
            activeSessionUseCase = get(),
            sessionServiceController = get(),
            sessionUiMapper = get(),
            errorMessageMapper = get(PresentationModuleQualifier.SessionErrorMessageMapper),
        )
    }
    single<SessionsListUiMapper> {
        SessionsListUiMapperImpl()
    }
    viewModel {
        SessionsListViewModel(
            getAllSessionsUseCase = get(),
            mapper = get(),
        )
    }
    viewModel {
        SessionCompletionViewModel(
            getSessionByIdUseCase = get(),
            sessionUiMapper = get(),
            errorMessageMapper = get(PresentationModuleQualifier.SessionErrorMessageMapper),
            savedStateHandle = get(),
        )
    }
}
