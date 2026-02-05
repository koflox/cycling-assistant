package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.domain.usecase.CheckLocationEnabledUseCase
import com.koflox.destinations.domain.usecase.CheckLocationEnabledUseCaseImpl
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCaseImpl
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCaseImpl
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCaseImpl
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<CheckLocationEnabledUseCase> {
        CheckLocationEnabledUseCaseImpl(
            locationSettingsDataSource = get(),
        )
    }
    factory<GetDestinationInfoUseCase> {
        GetDestinationInfoUseCaseImpl(
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
}
