package com.koflox.settings.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        SettingsViewModel(
            observeThemeUseCase = get(),
            updateThemeUseCase = get(),
            observeLocaleUseCase = get(),
            updateLocaleUseCase = get(),
            getRiderWeightUseCase = get(),
            updateRiderWeightUseCase = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
}
