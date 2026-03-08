package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
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
import org.koin.dsl.module

internal val domainModule = module {
    factory<GetNearbyDestinationsUseCase> {
        GetNearbyDestinationsUseCaseImpl(
            repository = get(),
            distanceCalculator = get(),
        )
    }
    single<ToleranceCalculator> { ToleranceCalculatorImpl() }
    factory<GetDestinationInfoUseCase> {
        GetDestinationInfoUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            repository = get(),
            getNearbyDestinationsUseCase = get(),
            distanceCalculator = get(),
            toleranceCalculator = get(),
        )
    }
    factory<InitializeDatabaseUseCase> {
        InitializeDatabaseUseCaseImpl(
            repository = get(),
        )
    }
    factory<GetDistanceBoundsUseCase> {
        GetDistanceBoundsUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            getNearbyDestinationsUseCase = get(),
            distanceCalculator = get(),
        )
    }
    factory<ObserveRidingModeUseCase> {
        ObserveRidingModeUseCaseImpl(
            repository = get(),
        )
    }
    factory<UpdateRidingModeUseCase> {
        UpdateRidingModeUseCaseImpl(
            repository = get(),
        )
    }
}
