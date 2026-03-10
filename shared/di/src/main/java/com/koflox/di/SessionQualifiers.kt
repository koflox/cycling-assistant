package com.koflox.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionDaoFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionMutex

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionErrorMapper
