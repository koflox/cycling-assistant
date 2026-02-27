package com.koflox.poi.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.poi.presentation.buttons.ActivePoiButtonsViewModel
import com.koflox.poi.presentation.selection.PoiSelectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        PoiSelectionViewModel(
            observeSelectedPoisUseCase = get(),
            updateSelectedPoisUseCase = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
    viewModel {
        ActivePoiButtonsViewModel(
            observeSelectedPoisUseCase = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
}
