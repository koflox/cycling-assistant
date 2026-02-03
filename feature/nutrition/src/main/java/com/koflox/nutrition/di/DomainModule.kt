package com.koflox.nutrition.di

import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<ObserveNutritionEventsUseCase> {
        ObserveNutritionEventsUseCaseImpl(
            sessionElapsedTimeUseCase = get(),
        )
    }
}
