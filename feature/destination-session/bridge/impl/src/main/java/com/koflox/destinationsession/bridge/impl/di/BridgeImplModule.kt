package com.koflox.destinationsession.bridge.impl.di

import com.koflox.destinationsession.bridge.impl.navigator.CyclingSessionUiNavigatorImpl
import com.koflox.destinationsession.bridge.impl.usecase.CyclingSessionUseCaseImpl
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import org.koin.dsl.module

val bridgeImplModule = module {
    factory<CyclingSessionUiNavigator> {
        CyclingSessionUiNavigatorImpl()
    }
    factory<CyclingSessionUseCase> {
        CyclingSessionUseCaseImpl(
            activeSessionUseCase = get(),
        )
    }
}
