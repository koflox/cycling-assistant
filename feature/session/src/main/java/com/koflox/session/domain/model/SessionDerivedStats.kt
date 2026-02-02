package com.koflox.session.domain.model

data class SessionDerivedStats(
    val idleTimeMs: Long,
    val movingTimeMs: Long,
    val altitudeLossMeters: Double,
    val caloriesBurned: Double,
)
