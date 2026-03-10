package com.koflox.destinationsession.bridge.impl.di

import com.koflox.destinationsession.bridge.impl.navigator.CyclingSessionUiNavigatorImpl
import com.koflox.destinationsession.bridge.impl.usecase.CyclingSessionUseCaseImpl
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.ObserveActiveSessionRouteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideCyclingSessionUiNavigator(): CyclingSessionUiNavigator = CyclingSessionUiNavigatorImpl()

    @Provides
    fun provideCyclingSessionUseCase(
        activeSessionUseCase: ActiveSessionUseCase,
        observeActiveSessionRouteUseCase: ObserveActiveSessionRouteUseCase,
    ): CyclingSessionUseCase = CyclingSessionUseCaseImpl(
        activeSessionUseCase = activeSessionUseCase,
        observeActiveSessionRouteUseCase = observeActiveSessionRouteUseCase,
    )
}
