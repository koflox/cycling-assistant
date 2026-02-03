package com.koflox.theme.di

import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import com.koflox.theme.domain.usecase.ObserveThemeUseCaseImpl
import com.koflox.theme.domain.usecase.UpdateThemeUseCase
import com.koflox.theme.domain.usecase.UpdateThemeUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<ObserveThemeUseCase> {
        ObserveThemeUseCaseImpl(repository = get())
    }
    factory<UpdateThemeUseCase> {
        UpdateThemeUseCaseImpl(repository = get())
    }
}
