package com.koflox.nutrition.di

import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCaseImpl
import com.koflox.nutrition.domain.usecase.ObserveNutritionSettingsUseCase
import com.koflox.nutrition.domain.usecase.ObserveNutritionSettingsUseCaseImpl
import com.koflox.nutrition.domain.usecase.UpdateNutritionSettingsUseCase
import com.koflox.nutrition.domain.usecase.UpdateNutritionSettingsUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<ObserveNutritionSettingsUseCase> {
        ObserveNutritionSettingsUseCaseImpl(
            repository = get(),
        )
    }
    factory<UpdateNutritionSettingsUseCase> {
        UpdateNutritionSettingsUseCaseImpl(
            repository = get(),
        )
    }
    factory<ObserveNutritionEventsUseCase> {
        ObserveNutritionEventsUseCaseImpl(
            sessionElapsedTimeUseCase = get(),
            observeNutritionSettingsUseCase = get(),
        )
    }
}
