package com.koflox.connectionsession.bridge.impl.di

import com.koflox.connectionsession.bridge.impl.usecase.SessionPowerMeterUseCaseImpl
import com.koflox.connectionsession.bridge.usecase.SessionPowerMeterUseCase
import org.koin.dsl.module

val connectionSessionBridgeImplModule = module {
    factory<SessionPowerMeterUseCase> {
        SessionPowerMeterUseCaseImpl(
            observePairedDevicesUseCase = get(),
            observePowerDataUseCase = get(),
        )
    }
}
