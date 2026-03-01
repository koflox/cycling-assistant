package com.koflox.connections.di

import org.koin.dsl.module

val connectionsModule = module {
    includes(
        dataModules + listOf(
            domainModule,
            presentationModule,
        ),
    )
}
