package com.koflox.destinations.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.destinations.presentation.destinations.DestinationsViewModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinations.presentation.mapper.DestinationUiMapperImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule = module {
    viewModelOf(::DestinationsViewModel)
    single<DestinationUiMapper> {
        DestinationUiMapperImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            distanceCalculator = get(),
        )
    }
}
