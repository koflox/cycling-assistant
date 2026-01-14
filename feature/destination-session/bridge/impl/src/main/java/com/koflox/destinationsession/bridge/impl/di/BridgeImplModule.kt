package com.koflox.destinationsession.bridge.impl.di

import com.koflox.destinationsession.bridge.DestinationSessionBridge
import com.koflox.destinationsession.bridge.impl.DestinationSessionBridgeImpl
import org.koin.dsl.module

val bridgeImplModule = module {
    single<DestinationSessionBridge> {
        DestinationSessionBridgeImpl()
    }
}
