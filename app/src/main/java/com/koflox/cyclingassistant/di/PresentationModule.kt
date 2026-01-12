package com.koflox.cyclingassistant.di

import com.koflox.cyclingassistant.presentation.destinations.DestinationsViewModel
import com.koflox.cyclingassistant.presentation.mapper.DestinationUiMapper
import com.koflox.cyclingassistant.presentation.mapper.DestinationUiMapperImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::DestinationsViewModel)
    single<DestinationUiMapper> {
        DestinationUiMapperImpl(
            distanceCalculator = get(),
        )
    }
}
