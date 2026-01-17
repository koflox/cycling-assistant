package com.koflox.session.di

import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCaseImpl
import com.koflox.session.domain.usecase.SessionTransitionUseCase
import com.koflox.session.domain.usecase.SessionTransitionUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    single<ActiveSessionUseCase> {
        ActiveSessionUseCaseImpl(
            sessionRepository = get(),
        )
    }
    factory<SessionTransitionUseCase> {
        SessionTransitionUseCaseImpl(
            sessionRepository = get(),
            distanceCalculator = get(),
            idGenerator = get(),
        )
    }
}
