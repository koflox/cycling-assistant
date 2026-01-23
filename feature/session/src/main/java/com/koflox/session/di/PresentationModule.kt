package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.di.ClassNameQualifier
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.presentation.completion.SessionCompletionViewModel
import com.koflox.session.presentation.error.SessionErrorMessageMapper
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.mapper.SessionUiMapperImpl
import com.koflox.session.presentation.session.timer.SessionTimerFactory
import com.koflox.session.presentation.session.timer.SessionTimerImpl
import com.koflox.session.presentation.session.SessionViewModel
import com.koflox.session.presentation.sessionslist.SessionsListUiMapper
import com.koflox.session.presentation.sessionslist.SessionsListUiMapperImpl
import com.koflox.session.presentation.sessionslist.SessionsListViewModel
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.SessionImageSharerImpl
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.ShareErrorMapperImpl
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
    single<SessionTimerFactory> {
        SessionTimerFactory { scope -> SessionTimerImpl(scope) }
    }
    viewModel {
        SessionViewModel(
            createSessionUseCase = get(),
            updateSessionStatusUseCase = get(),
            activeSessionUseCase = get(),
            sessionServiceController = get(),
            sessionUiMapper = get(),
            errorMessageMapper = get(PresentationModuleQualifier.SessionErrorMessageMapper),
            sessionTimerFactory = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
    single<SessionsListUiMapper> {
        SessionsListUiMapperImpl()
    }
    single<SessionImageSharer> {
        SessionImageSharerImpl(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
    single<ShareErrorMapper> {
        ShareErrorMapperImpl(context = androidContext())
    }
    viewModel {
        SessionsListViewModel(
            getAllSessionsUseCase = get(),
            getSessionByIdUseCase = get(),
            mapper = get(),
            sessionUiMapper = get(),
            imageSharer = get(),
            shareErrorMapper = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
    viewModel {
        SessionCompletionViewModel(
            getSessionByIdUseCase = get(),
            sessionUiMapper = get(),
            errorMessageMapper = get(PresentationModuleQualifier.SessionErrorMessageMapper),
            imageSharer = get(),
            shareErrorMapper = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
            savedStateHandle = get(),
        )
    }
}
