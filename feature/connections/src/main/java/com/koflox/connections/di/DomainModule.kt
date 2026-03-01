package com.koflox.connections.di

import com.koflox.connections.domain.usecase.DeletePairedDeviceUseCase
import com.koflox.connections.domain.usecase.DeletePairedDeviceUseCaseImpl
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCaseImpl
import com.koflox.connections.domain.usecase.SavePairedDeviceUseCase
import com.koflox.connections.domain.usecase.SavePairedDeviceUseCaseImpl
import com.koflox.connections.domain.usecase.UpdateDeviceSessionUsageUseCase
import com.koflox.connections.domain.usecase.UpdateDeviceSessionUsageUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<ObservePairedDevicesUseCase> {
        ObservePairedDevicesUseCaseImpl(repository = get())
    }
    factory<SavePairedDeviceUseCase> {
        SavePairedDeviceUseCaseImpl(
            repository = get(),
            idGenerator = get(),
        )
    }
    factory<DeletePairedDeviceUseCase> {
        DeletePairedDeviceUseCaseImpl(repository = get())
    }
    factory<UpdateDeviceSessionUsageUseCase> {
        UpdateDeviceSessionUsageUseCaseImpl(repository = get())
    }
}
