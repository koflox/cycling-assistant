package com.koflox.destinationnutrition.bridge.impl.di

import com.koflox.destinationnutrition.bridge.impl.navigator.NutritionUiNavigatorImpl
import com.koflox.destinationnutrition.bridge.impl.usecase.ObserveNutritionBreakUseCaseImpl
import com.koflox.destinationnutrition.bridge.navigator.NutritionUiNavigator
import com.koflox.destinationnutrition.bridge.usecase.ObserveNutritionBreakUseCase
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideNutritionUiNavigator(): NutritionUiNavigator = NutritionUiNavigatorImpl()

    @Provides
    fun provideObserveNutritionBreakUseCase(
        observeNutritionEventsUseCase: ObserveNutritionEventsUseCase,
    ): ObserveNutritionBreakUseCase = ObserveNutritionBreakUseCaseImpl(
        observeNutritionEventsUseCase = observeNutritionEventsUseCase,
    )
}
