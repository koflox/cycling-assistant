package com.koflox.observability.performance

import com.koflox.observability.FirebaseServicesProvider

internal class PerformanceMonitorImpl(
    private val firebaseServicesProvider: FirebaseServicesProvider,
) : PerformanceMonitor {

    override suspend fun setCollectionEnabled(enabled: Boolean) {
        firebaseServicesProvider.getPerformance().isPerformanceCollectionEnabled = enabled
    }
}
