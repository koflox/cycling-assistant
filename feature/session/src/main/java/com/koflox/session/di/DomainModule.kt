package com.koflox.session.di

import com.koflox.session.domain.usecase.SessionTransitionUseCase
import com.koflox.session.domain.usecase.SessionTransitionUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<SessionTransitionUseCase> {
        SessionTransitionUseCaseImpl(
            sessionRepository = get(),
        )
    }
}
