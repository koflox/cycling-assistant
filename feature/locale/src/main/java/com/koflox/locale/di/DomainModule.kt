package com.koflox.locale.di

import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.locale.domain.usecase.ObserveLocaleUseCaseImpl
import com.koflox.locale.domain.usecase.UpdateLocaleUseCase
import com.koflox.locale.domain.usecase.UpdateLocaleUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<ObserveLocaleUseCase> {
        ObserveLocaleUseCaseImpl(repository = get())
    }
    factory<UpdateLocaleUseCase> {
        UpdateLocaleUseCaseImpl(repository = get())
    }
}
