package com.koflox.connections.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.connections.presentation.error.ConnectionsErrorMessageMapper
import com.koflox.connections.presentation.listing.DeviceListViewModel
import com.koflox.connections.presentation.scanning.BleScanningViewModel
import com.koflox.error.mapper.ErrorMessageMapper
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    single<ErrorMessageMapper>(ConnectionsErrorMapperQualifier) {
        ConnectionsErrorMessageMapper(
            defaultMapper = get(),
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
        )
    }
    viewModel {
        DeviceListViewModel(
            observePairedDevicesUseCase = get(),
            deletePairedDeviceUseCase = get(),
            updateDeviceSessionUsageUseCase = get(),
            bluetoothStateMonitor = get(),
            errorMessageMapper = get(ConnectionsErrorMapperQualifier),
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
        )
    }
    viewModel {
        BleScanningViewModel(
            bleScanner = get(),
            blePermissionChecker = get(),
            observePairedDevicesUseCase = get(),
            savePairedDeviceUseCase = get(),
            errorMessageMapper = get(ConnectionsErrorMapperQualifier),
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
        )
    }
}

private object ConnectionsErrorMapperQualifier : org.koin.core.qualifier.Qualifier {
    override val value: String = "ConnectionsErrorMessageMapper"
}
