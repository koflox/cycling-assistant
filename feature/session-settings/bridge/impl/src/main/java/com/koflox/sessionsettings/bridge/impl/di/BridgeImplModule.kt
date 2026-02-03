package com.koflox.sessionsettings.bridge.impl.di

import com.koflox.sessionsettings.bridge.api.RiderProfileUseCase
import com.koflox.sessionsettings.bridge.impl.usecase.RiderProfileUseCaseImpl
import org.koin.dsl.module

val sessionSettingsBridgeImplModule = module {
    factory<RiderProfileUseCase> {
        RiderProfileUseCaseImpl(
            getRiderWeightUseCase = get(),
        )
    }
}
