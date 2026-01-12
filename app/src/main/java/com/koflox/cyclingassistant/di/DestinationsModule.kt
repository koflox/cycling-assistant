package com.koflox.cyclingassistant.di

import org.koin.dsl.module

internal val destinationModule = module {
    includes(domainModule, presentationModule)
    includes(dataModules)
}
