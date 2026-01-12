package com.koflox.cyclingassistant.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.cyclingassistant.domain.usecase.GetRandomDestinationUseCase
import com.koflox.cyclingassistant.domain.usecase.GetRandomDestinationUseCaseImpl
import com.koflox.cyclingassistant.domain.usecase.GetUserLocationUseCase
import com.koflox.cyclingassistant.domain.usecase.GetUserLocationUseCaseImpl
import com.koflox.cyclingassistant.domain.usecase.InitializeDatabaseUseCase
import com.koflox.cyclingassistant.domain.usecase.InitializeDatabaseUseCaseImpl
import com.koflox.cyclingassistant.domain.util.DistanceCalculator
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
    single {
        DistanceCalculator()
    }
}
