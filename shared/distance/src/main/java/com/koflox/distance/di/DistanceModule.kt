package com.koflox.distance.di

import com.koflox.distance.DefaultDistanceCalculator
import com.koflox.distance.DistanceCalculator
import org.koin.dsl.module

val distanceModule = module {
    single<DistanceCalculator> { DefaultDistanceCalculator() }
}
