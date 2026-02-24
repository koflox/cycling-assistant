package com.koflox.settings.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.settings.presentation.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        SettingsViewModel(
            application = androidApplication(),
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
