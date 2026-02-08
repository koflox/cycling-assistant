package com.koflox.destinationnutrition.bridge.impl.di

import com.koflox.destinationnutrition.bridge.impl.navigator.NutritionUiNavigatorImpl
import com.koflox.destinationnutrition.bridge.impl.usecase.ObserveNutritionBreakUseCaseImpl
import com.koflox.destinationnutrition.bridge.navigator.NutritionUiNavigator
import com.koflox.destinationnutrition.bridge.usecase.ObserveNutritionBreakUseCase
import org.koin.dsl.module

val destinationNutritionBridgeImplModule = module {
    factory<NutritionUiNavigator> {
        NutritionUiNavigatorImpl()
    }
    factory<ObserveNutritionBreakUseCase> {
        ObserveNutritionBreakUseCaseImpl(
            observeNutritionEventsUseCase = get(),
        )
    }
}
