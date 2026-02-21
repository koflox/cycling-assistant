package com.koflox.concurrent

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class ConcurrentFactory<T : Any> {

    @Volatile
    private var instance: T? = null
    private val mutex = Mutex()

    suspend fun get(): T {
        instance?.let { return it }
        return mutex.withLock {
            instance ?: create().also { instance = it }
        }
    }

    protected abstract suspend fun create(): T
}
