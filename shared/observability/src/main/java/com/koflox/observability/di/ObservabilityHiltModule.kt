package com.koflox.observability.di

import com.koflox.observability.FirebaseProvider
import com.koflox.observability.FirebaseServicesProvider
import com.koflox.observability.FirebaseServicesProviderImpl
import com.koflox.observability.performance.PerformanceMonitor
import com.koflox.observability.performance.PerformanceMonitorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ObservabilityHiltModule {

    @Provides
    @Singleton
    fun provideFirebaseServicesProvider(): FirebaseServicesProvider =
        FirebaseServicesProviderImpl(
            firebaseFactory = FirebaseProvider.getFactory(),
        )

    @Provides
    @Singleton
    fun providePerformanceMonitor(
        firebaseServicesProvider: FirebaseServicesProvider,
    ): PerformanceMonitor = PerformanceMonitorImpl(
        firebaseServicesProvider = firebaseServicesProvider,
    )
}
