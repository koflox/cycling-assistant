package com.koflox.nutritionsession.bridge.impl.di

import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import com.koflox.nutritionsession.bridge.impl.usecase.NutritionReminderUseCaseImpl
import com.koflox.nutritionsession.bridge.impl.usecase.SessionElapsedTimeUseCaseImpl
import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideNutritionReminderUseCase(
        observeNutritionEventsUseCase: ObserveNutritionEventsUseCase,
    ): NutritionReminderUseCase = NutritionReminderUseCaseImpl(
        observeNutritionEventsUseCase = observeNutritionEventsUseCase,
    )

    @Provides
    fun provideSessionElapsedTimeUseCase(
        activeSessionUseCase: ActiveSessionUseCase,
    ): SessionElapsedTimeUseCase = SessionElapsedTimeUseCaseImpl(
        activeSessionUseCase = activeSessionUseCase,
    )
}
