package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.presentation.destinations.DestinationsViewModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinations.presentation.mapper.DestinationUiMapperImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        DestinationsViewModel(
            checkLocationEnabledUseCase = get(),
            getUserLocationUseCase = get(),
            observeUserLocationUseCase = get(),
            initializeDatabaseUseCase = get(),
            getDestinationInfoUseCase = get(),
            distanceCalculator = get(),
            uiMapper = get(),
            application = androidApplication(),
            cyclingSessionUseCase = get(),
            observeNutritionBreakUseCase = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
    single<DestinationUiMapper> {
        DestinationUiMapperImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            distanceCalculator = get(),
            context = androidApplication(),
        )
    }
}
