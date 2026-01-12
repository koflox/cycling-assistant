package com.koflox.cyclingassistant.app

import com.koflox.concurrent.concurrentModule
import com.koflox.cyclingassistant.di.destinationModule
import org.koin.dsl.module

internal val appModule = module {
    includes(
        concurrentModule,
        destinationModule,
    )
}
