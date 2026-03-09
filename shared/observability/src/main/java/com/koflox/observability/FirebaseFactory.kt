package com.koflox.observability

import android.app.Application
import android.os.Looper
import android.util.Log
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class FirebaseFactory(
    private val app: Application,
    private val ioDispatcher: CoroutineDispatcher,
) {

    companion object {
        private const val TAG = "FirebaseFactory"
    }

    private val mutex = Mutex()

    @Volatile
    private var firebaseApp: FirebaseApp? = null

    suspend fun create(): FirebaseApp {
        firebaseApp?.let { return it }
        mutex.withLock {
            if (firebaseApp == null) {
                withContext(ioDispatcher) {
                    Log.d(TAG, "Firebase initialization starting on ${Thread.currentThread().name}")
                    Looper.myLooper() ?: Looper.prepare()
                    firebaseApp = FirebaseApp.initializeApp(app)
                    Looper.myLooper()?.quitSafely()
                }
                Log.d(TAG, "Firebase initialized: ${firebaseApp != null}")
            }
        }
        return requireNotNull(firebaseApp)
    }
}
