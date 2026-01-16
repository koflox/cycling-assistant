package com.koflox.session.di

import org.koin.dsl.module

val sessionModule = module {
    includes(domainModule, presentationModule)
    includes(dataModules)
}
