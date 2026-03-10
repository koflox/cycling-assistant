package com.koflox.destinations.di

import com.koflox.destinations.domain.repository.DestinationsRepository
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCaseImpl
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCase
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCaseImpl
import com.koflox.destinations.domain.usecase.GetNearbyDestinationsUseCase
import com.koflox.destinations.domain.usecase.GetNearbyDestinationsUseCaseImpl
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCaseImpl
import com.koflox.destinations.domain.usecase.ObserveRidingModeUseCase
import com.koflox.destinations.domain.usecase.ObserveRidingModeUseCaseImpl
import com.koflox.destinations.domain.usecase.ToleranceCalculator
import com.koflox.destinations.domain.usecase.ToleranceCalculatorImpl
import com.koflox.destinations.domain.usecase.UpdateRidingModeUseCase
import com.koflox.destinations.domain.usecase.UpdateRidingModeUseCaseImpl
import com.koflox.di.DefaultDispatcher
import com.koflox.distance.DistanceCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DomainModule {

    @Provides
    fun provideGetNearbyDestinationsUseCase(
        repository: DestinationsRepository,
        distanceCalculator: DistanceCalculator,
    ): GetNearbyDestinationsUseCase = GetNearbyDestinationsUseCaseImpl(
        repository = repository,
        distanceCalculator = distanceCalculator,
    )

    @Provides
    @Singleton
    fun provideToleranceCalculator(): ToleranceCalculator = ToleranceCalculatorImpl()

    @Provides
    fun provideGetDestinationInfoUseCase(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        repository: DestinationsRepository,
        getNearbyDestinationsUseCase: GetNearbyDestinationsUseCase,
        distanceCalculator: DistanceCalculator,
        toleranceCalculator: ToleranceCalculator,
    ): GetDestinationInfoUseCase = GetDestinationInfoUseCaseImpl(
        dispatcherDefault = dispatcherDefault,
        repository = repository,
        getNearbyDestinationsUseCase = getNearbyDestinationsUseCase,
        distanceCalculator = distanceCalculator,
        toleranceCalculator = toleranceCalculator,
    )

    @Provides
    fun provideInitializeDatabaseUseCase(
        repository: DestinationsRepository,
    ): InitializeDatabaseUseCase = InitializeDatabaseUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideGetDistanceBoundsUseCase(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        getNearbyDestinationsUseCase: GetNearbyDestinationsUseCase,
        distanceCalculator: DistanceCalculator,
    ): GetDistanceBoundsUseCase = GetDistanceBoundsUseCaseImpl(
        dispatcherDefault = dispatcherDefault,
        getNearbyDestinationsUseCase = getNearbyDestinationsUseCase,
        distanceCalculator = distanceCalculator,
    )

    @Provides
    fun provideObserveRidingModeUseCase(
        repository: RidePreferencesRepository,
    ): ObserveRidingModeUseCase = ObserveRidingModeUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateRidingModeUseCase(
        repository: RidePreferencesRepository,
    ): UpdateRidingModeUseCase = UpdateRidingModeUseCaseImpl(
        repository = repository,
    )
}
