package com.koflox.connectionsession.bridge.impl.di

import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connectionsession.bridge.impl.usecase.SessionPowerMeterUseCaseImpl
import com.koflox.connectionsession.bridge.usecase.SessionPowerMeterUseCase
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideSessionPowerMeterUseCase(
        observePairedDevicesUseCase: ObservePairedDevicesUseCase,
        observePowerDataUseCase: ObservePowerDataUseCase,
    ): SessionPowerMeterUseCase = SessionPowerMeterUseCaseImpl(
        observePairedDevicesUseCase = observePairedDevicesUseCase,
        observePowerDataUseCase = observePowerDataUseCase,
    )
}
