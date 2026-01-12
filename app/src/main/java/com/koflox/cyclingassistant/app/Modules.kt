package com.koflox.cyclingassistant.app

import com.koflox.concurrent.concurrentModule
import com.koflox.destinations.di.destinationsModule
import com.koflox.location.locationModule
import org.koin.dsl.module

internal val appModule = module {
    includes(
        concurrentModule,
        locationModule,
        destinationsModule,
    )
}
