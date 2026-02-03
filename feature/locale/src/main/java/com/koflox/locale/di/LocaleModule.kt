package com.koflox.locale.di

import org.koin.dsl.module

val localeModule = module {
    includes(dataModules + domainModule)
}
