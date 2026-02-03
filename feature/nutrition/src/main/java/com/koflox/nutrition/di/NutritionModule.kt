package com.koflox.nutrition.di

import org.koin.dsl.module

val nutritionModule = module {
    includes(
        domainModule,
    )
}
