package com.koflox.sensor.power.di

import org.koin.dsl.module

val powerSensorModule = module {
    includes(
        domainModule,
        presentationModule,
    )
}
