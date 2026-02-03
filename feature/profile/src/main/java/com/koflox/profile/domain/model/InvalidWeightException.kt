package com.koflox.profile.domain.model

class InvalidWeightException(
    val minWeightKg: Double,
    val maxWeightKg: Double,
) : Exception("Weight must be between $minWeightKg and $maxWeightKg kg")
