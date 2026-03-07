package com.koflox.concurrent

fun interface CurrentTimeProvider {
    fun currentTimeMs(): Long
}

internal class SystemCurrentTimeProvider : CurrentTimeProvider {
    override fun currentTimeMs(): Long = System.currentTimeMillis()
}
