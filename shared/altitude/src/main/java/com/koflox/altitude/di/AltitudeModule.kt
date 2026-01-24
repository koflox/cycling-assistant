package com.koflox.altitude.di

import com.koflox.altitude.AltitudeCalculator
import com.koflox.altitude.DefaultAltitudeCalculator
import org.koin.dsl.module

val altitudeModule = module {
    single<AltitudeCalculator> { DefaultAltitudeCalculator() }
}
