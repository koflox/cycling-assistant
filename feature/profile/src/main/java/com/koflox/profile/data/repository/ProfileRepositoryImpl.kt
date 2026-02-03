package com.koflox.profile.data.repository

import com.koflox.profile.data.source.ProfileLocalDataSource
import com.koflox.profile.domain.repository.ProfileRepository

internal class ProfileRepositoryImpl(
    private val localDataSource: ProfileLocalDataSource,
) : ProfileRepository {
    override suspend fun getRiderWeightKg(): Float? = localDataSource.getRiderWeightKg()

    override suspend fun setRiderWeightKg(weightKg: Double) {
        localDataSource.setRiderWeightKg(weightKg)
    }
}
