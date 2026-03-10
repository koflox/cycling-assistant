package com.koflox.sensor.power.di

import com.koflox.ble.connection.BleGattManager
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.di.IoDispatcher
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCaseImpl
import com.koflox.sensorprotocol.power.CadenceCalculator
import com.koflox.sensorprotocol.power.CyclingPowerParser
import com.koflox.sensorprotocol.power.WheelSpeedCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PowerSensorModule {

    @Provides
    @Singleton
    fun provideCyclingPowerParser(): CyclingPowerParser = CyclingPowerParser()

    @Provides
    fun provideCadenceCalculator(): CadenceCalculator = CadenceCalculator()

    @Provides
    fun provideWheelSpeedCalculator(): WheelSpeedCalculator = WheelSpeedCalculator()

    @Provides
    fun provideObservePowerDataUseCase(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        gattManager: BleGattManager,
        parser: CyclingPowerParser,
        cadenceCalculator: CadenceCalculator,
        wheelSpeedCalculator: WheelSpeedCalculator,
        currentTimeProvider: CurrentTimeProvider,
    ): ObservePowerDataUseCase = ObservePowerDataUseCaseImpl(
        dispatcherIo = dispatcherIo,
        gattManager = gattManager,
        parser = parser,
        cadenceCalculator = cadenceCalculator,
        wheelSpeedCalculator = wheelSpeedCalculator,
        currentTimeProvider = currentTimeProvider,
    )
}
