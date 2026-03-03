package com.koflox.sensor.power.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCaseImpl
import com.koflox.sensorprotocol.power.CadenceCalculator
import com.koflox.sensorprotocol.power.CyclingPowerParser
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

internal val domainModule = module {
    single<CyclingPowerParser> { CyclingPowerParser() }
    factory<CadenceCalculator> { CadenceCalculator() }
    factory<ObservePowerDataUseCase> {
        ObservePowerDataUseCaseImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            gattManager = get(),
            parser = get(),
            cadenceCalculator = get(),
        )
    }
}
