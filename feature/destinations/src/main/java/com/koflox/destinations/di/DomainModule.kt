package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.domain.usecase.GetRandomDestinationUseCase
import com.koflox.destinations.domain.usecase.GetRandomDestinationUseCaseImpl
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCaseImpl
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCaseImpl
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCaseImpl
import com.koflox.destinations.domain.util.DistanceCalculator
import org.koin.dsl.module

internal val domainModule = module {
    factory<GetRandomDestinationUseCase> {
        GetRandomDestinationUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            repository = get(),
            distanceCalculator = get(),
        )
    }
    factory<GetUserLocationUseCase> {
        GetUserLocationUseCaseImpl(
            repository = get(),
        )
    }
    factory<InitializeDatabaseUseCase> {
        InitializeDatabaseUseCaseImpl(
            repository = get(),
        )
    }
    factory<ObserveUserLocationUseCase> {
        ObserveUserLocationUseCaseImpl(
            repository = get(),
        )
    }
    single {
        DistanceCalculator(
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
}
