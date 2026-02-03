package com.koflox.profile.data.source

internal interface ProfileLocalDataSource {
    suspend fun getRiderWeightKg(): Float?
    suspend fun setRiderWeightKg(weightKg: Double)
}
