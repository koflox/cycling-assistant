package com.koflox.destinations.di

import org.koin.dsl.module

val destinationsModule = module {
    includes(domainModule, presentationModule)
    includes(dataModules)
}
