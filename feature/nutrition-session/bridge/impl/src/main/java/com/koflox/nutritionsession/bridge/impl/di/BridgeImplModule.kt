package com.koflox.nutritionsession.bridge.impl.di

import com.koflox.nutritionsession.bridge.impl.usecase.SessionElapsedTimeUseCaseImpl
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import org.koin.dsl.module

val nutritionSessionBridgeImplModule = module {
    factory<SessionElapsedTimeUseCase> {
        SessionElapsedTimeUseCaseImpl(
            activeSessionUseCase = get(),
        )
    }
}
