package com.koflox.theme.di

import org.koin.dsl.module

val themeModule = module {
    includes(dataModules + domainModule)
}
