package com.koflox.poi.di

import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCase
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCaseImpl
import com.koflox.poi.domain.usecase.UpdateSelectedPoisUseCase
import com.koflox.poi.domain.usecase.UpdateSelectedPoisUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<ObserveSelectedPoisUseCase> {
        ObserveSelectedPoisUseCaseImpl(repository = get())
    }
    factory<UpdateSelectedPoisUseCase> {
        UpdateSelectedPoisUseCaseImpl(repository = get())
    }
}
