package com.koflox.profile.domain.usecase

import com.koflox.profile.domain.repository.ProfileRepository

interface GetRiderWeightUseCase {
    suspend fun getRiderWeightKg(): Float?
}

internal class GetRiderWeightUseCaseImpl(
    private val repository: ProfileRepository,
) : GetRiderWeightUseCase {
    override suspend fun getRiderWeightKg(): Float? = repository.getRiderWeightKg()
}
