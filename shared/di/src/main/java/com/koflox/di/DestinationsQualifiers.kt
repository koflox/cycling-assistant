package com.koflox.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DestinationsDaoFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DestinationFilesMutex
