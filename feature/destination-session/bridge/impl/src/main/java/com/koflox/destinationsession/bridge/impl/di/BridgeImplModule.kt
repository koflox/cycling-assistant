package com.koflox.destinationsession.bridge.impl.di

import com.koflox.destinationsession.bridge.CyclingSessionUiNavigator
import com.koflox.destinationsession.bridge.CyclingSessionUseCase
import com.koflox.destinationsession.bridge.DestinationSessionBridge
import com.koflox.destinationsession.bridge.impl.DestinationSessionBridgeImpl
import org.koin.dsl.binds
import org.koin.dsl.module

val bridgeImplModule = module {
    single {
        DestinationSessionBridgeImpl(
            activeSessionUseCase = get(),
        )
    } binds arrayOf(
        DestinationSessionBridge::class,
        CyclingSessionUseCase::class,
        CyclingSessionUiNavigator::class,
    )
}
