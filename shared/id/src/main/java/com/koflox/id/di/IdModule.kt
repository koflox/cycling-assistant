package com.koflox.id.di

import com.koflox.id.IdGenerator
import com.koflox.id.UuidIdGenerator
import org.koin.dsl.module

val idModule = module {
    single<IdGenerator> { UuidIdGenerator() }
}
