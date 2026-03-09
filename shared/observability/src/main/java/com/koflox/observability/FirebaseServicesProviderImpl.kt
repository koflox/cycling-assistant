package com.koflox.observability

import com.google.firebase.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.performance

internal class FirebaseServicesProviderImpl(
    private val firebaseFactory: FirebaseFactory,
) : FirebaseServicesProvider {

    override suspend fun getPerformance(): FirebasePerformance {
        firebaseFactory.create()
        return Firebase.performance
    }
}
