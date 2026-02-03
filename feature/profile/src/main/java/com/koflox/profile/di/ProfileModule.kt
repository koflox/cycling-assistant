package com.koflox.profile.di

import org.koin.dsl.module

val profileModule = module {
    includes(dataModules + domainModule)
}
