package com.koflox.profile.domain.repository

internal interface ProfileRepository {
    suspend fun getRiderWeightKg(): Float?
    suspend fun setRiderWeightKg(weightKg: Double)
}
