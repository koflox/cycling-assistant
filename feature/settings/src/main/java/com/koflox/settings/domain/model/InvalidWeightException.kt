package com.koflox.settings.domain.model

internal class InvalidWeightException(
    val minWeightKg: Double,
    val maxWeightKg: Double,
) : Exception("Weight must be between $minWeightKg and $maxWeightKg kg")
