package com.koflox.observability

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Entry point for Firebase initialization. Used **before** DI (Koin) is started.
 *
 * Called from [com.koflox.observability.initializer.FirebaseInitializer] during AndroidX Startup,
 * before `Application.onCreate()`. Initializes Firebase and configures data collection
 * based on build type configuration.
 *
 * After DI is available, use [FirebaseServicesProvider] to access Firebase services.
 */
internal object FirebaseProvider {

    @Volatile
    private var firebaseFactory: FirebaseFactory? = null

    fun initialize(application: Application, collectionEnabled: Boolean) {
        val factory = FirebaseFactory(application, Dispatchers.IO).also {
            firebaseFactory = it
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            factory.create()
            Firebase.crashlytics.isCrashlyticsCollectionEnabled = collectionEnabled
            Firebase.performance.isPerformanceCollectionEnabled = collectionEnabled
        }
    }

    fun getFactory(): FirebaseFactory {
        return requireNotNull(firebaseFactory) {
            "FirebaseProvider must be initialized before accessing FirebaseFactory"
        }
    }
}
