package com.koflox.nutrition.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.nutrition.presentation.settings.NutritionSettingsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        NutritionSettingsViewModel(
            observeNutritionSettingsUseCase = get(),
            updateNutritionSettingsUseCase = get(),
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
        )
    }
}
