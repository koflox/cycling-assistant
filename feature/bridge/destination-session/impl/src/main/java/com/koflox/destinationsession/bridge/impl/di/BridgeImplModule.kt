package com.koflox.destinationsession.bridge.impl.di

import com.koflox.destinationsession.bridge.impl.navigator.CyclingSessionUiNavigatorImpl
import com.koflox.destinationsession.bridge.impl.usecase.CyclingSessionUseCaseImpl
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import org.koin.dsl.module

val destinationSessionBridgeImplModule = module {
    factory<CyclingSessionUiNavigator> {
        CyclingSessionUiNavigatorImpl()
    }
    factory<CyclingSessionUseCase> {
        CyclingSessionUseCaseImpl(
            activeSessionUseCase = get(),
            createSessionUseCase = get(),
            sessionServiceController = get(),
        )
    }
}
