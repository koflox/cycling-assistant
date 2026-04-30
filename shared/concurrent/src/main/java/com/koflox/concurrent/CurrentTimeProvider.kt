package com.koflox.concurrent

fun interface CurrentTimeProvider {
    fun currentTimeMs(): Long
}

class SystemCurrentTimeProvider : CurrentTimeProvider {
    override fun currentTimeMs(): Long = System.currentTimeMillis()
}
