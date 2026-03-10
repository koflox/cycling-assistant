package com.koflox.nutrition.di

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.di.IoDispatcher
import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCaseImpl
import com.koflox.nutrition.domain.usecase.ObserveNutritionSettingsUseCase
import com.koflox.nutrition.domain.usecase.ObserveNutritionSettingsUseCaseImpl
import com.koflox.nutrition.domain.usecase.UpdateNutritionSettingsUseCase
import com.koflox.nutrition.domain.usecase.UpdateNutritionSettingsUseCaseImpl
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal object NutritionDomainHiltModule {

    @Provides
    fun provideObserveNutritionSettingsUseCase(
        repository: NutritionSettingsRepository,
    ): ObserveNutritionSettingsUseCase = ObserveNutritionSettingsUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateNutritionSettingsUseCase(
        repository: NutritionSettingsRepository,
    ): UpdateNutritionSettingsUseCase = UpdateNutritionSettingsUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideObserveNutritionEventsUseCase(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        sessionElapsedTimeUseCase: SessionElapsedTimeUseCase,
        observeNutritionSettingsUseCase: ObserveNutritionSettingsUseCase,
        currentTimeProvider: CurrentTimeProvider,
    ): ObserveNutritionEventsUseCase = ObserveNutritionEventsUseCaseImpl(
        dispatcherIo = dispatcherIo,
        sessionElapsedTimeUseCase = sessionElapsedTimeUseCase,
        observeNutritionSettingsUseCase = observeNutritionSettingsUseCase,
        currentTimeProvider = currentTimeProvider,
    )
}
