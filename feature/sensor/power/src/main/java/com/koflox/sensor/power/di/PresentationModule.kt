package com.koflox.sensor.power.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.sensor.power.presentation.testmode.PowerTestModeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        PowerTestModeViewModel(
            observePowerDataUseCase = get(),
            bluetoothStateMonitor = get(),
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            savedStateHandle = get(),
        )
    }
}
