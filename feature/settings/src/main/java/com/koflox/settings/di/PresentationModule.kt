package com.koflox.settings.di

import com.koflox.settings.domain.usecase.ObserveSettingsUseCase
import com.koflox.settings.domain.usecase.ObserveSettingsUseCaseImpl
import com.koflox.settings.domain.usecase.UpdateSettingsUseCase
import com.koflox.settings.domain.usecase.UpdateSettingsUseCaseImpl
import com.koflox.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    factory<ObserveSettingsUseCase> {
        ObserveSettingsUseCaseImpl(repository = get())
    }
    factory<UpdateSettingsUseCase> {
        UpdateSettingsUseCaseImpl(repository = get())
    }
    viewModel {
        SettingsViewModel(
            observeSettingsUseCase = get(),
            updateSettingsUseCase = get(),
        )
    }
}
