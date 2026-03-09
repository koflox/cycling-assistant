package com.koflox.observability.di

import com.koflox.observability.FirebaseProvider
import com.koflox.observability.FirebaseServicesProvider
import com.koflox.observability.FirebaseServicesProviderImpl
import com.koflox.observability.performance.PerformanceMonitor
import com.koflox.observability.performance.PerformanceMonitorImpl
import org.koin.dsl.module

val observabilityModule = module {
    single<FirebaseServicesProvider> {
        FirebaseServicesProviderImpl(
            firebaseFactory = FirebaseProvider.getFactory(),
        )
    }
    single<PerformanceMonitor> {
        PerformanceMonitorImpl(
            firebaseServicesProvider = get(),
        )
    }
}
