package com.koflox.session.tracking.di

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.session.presentation.session.timer.SessionTimerFactory
import com.koflox.session.presentation.session.timer.SessionTimerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TrackingPresentationHiltModule {

    @Provides
    @Singleton
    fun provideSessionTimerFactory(
        currentTimeProvider: CurrentTimeProvider,
    ): SessionTimerFactory = SessionTimerFactory { scope ->
        SessionTimerImpl(scope, currentTimeProvider)
    }
}
