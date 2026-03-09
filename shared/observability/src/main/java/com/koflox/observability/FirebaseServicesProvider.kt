package com.koflox.observability

import com.google.firebase.perf.FirebasePerformance

/**
 * Provides access to Firebase services. Used **after** DI (Koin) is started.
 *
 * Injected via Koin into components that need Firebase Performance.
 * Each method suspends until Firebase initialization (started by [FirebaseProvider]) completes.
 */
internal interface FirebaseServicesProvider {
    suspend fun getPerformance(): FirebasePerformance
}
