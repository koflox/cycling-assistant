package com.koflox.settings.di

import org.koin.dsl.module

val settingsModule = module {
    includes(dataModule, presentationModule)
}
