package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.presentation.destinations.RideMapViewModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinations.presentation.mapper.DestinationUiMapperImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        RideMapViewModel(
            checkLocationEnabledUseCase = get(),
            getUserLocationUseCase = get(),
            observeUserLocationUseCase = get(),
            initializeDatabaseUseCase = get(),
            getDestinationInfoUseCase = get(),
            getDistanceBoundsUseCase = get(),
            distanceCalculator = get(),
            uiMapper = get(),
            toleranceCalculator = get(),
            application = androidApplication(),
            cyclingSessionUseCase = get(),
            observeNutritionBreakUseCase = get(),
            observeRidingModeUseCase = get(),
            updateRidingModeUseCase = get(),
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
