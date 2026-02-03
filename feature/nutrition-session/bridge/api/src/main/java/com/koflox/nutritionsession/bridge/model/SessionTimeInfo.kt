package com.koflox.nutritionsession.bridge.model

data class SessionTimeInfo(
    val elapsedTimeMs: Long,
    val lastResumedTimeMs: Long,
    val isRunning: Boolean,
)
