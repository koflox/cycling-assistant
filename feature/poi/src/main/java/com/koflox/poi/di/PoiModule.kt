package com.koflox.poi.di

import org.koin.dsl.module

val poiModule = module {
    includes(dataModules + domainModule + presentationModule)
}
