package com.koflox.observability.performance

interface PerformanceMonitor {
    suspend fun setCollectionEnabled(enabled: Boolean)
}
