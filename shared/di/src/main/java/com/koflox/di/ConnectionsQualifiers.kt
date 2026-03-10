package com.koflox.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ConnectionsDaoFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ConnectionsErrorMapper
