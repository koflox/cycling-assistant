package com.koflox.profilesession.bridge.impl.di

import com.koflox.profilesession.bridge.api.RiderProfileUseCase
import com.koflox.profilesession.bridge.impl.usecase.RiderProfileUseCaseImpl
import org.koin.dsl.module

val profileSessionBridgeImplModule = module {
    factory<RiderProfileUseCase> {
        RiderProfileUseCaseImpl(
            getRiderWeightUseCase = get(),
        )
    }
}
