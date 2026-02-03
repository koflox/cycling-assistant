package com.koflox.profile.domain.usecase

import com.koflox.profile.domain.model.InvalidWeightException
import com.koflox.profile.domain.repository.ProfileRepository

interface UpdateRiderWeightUseCase {
    suspend fun updateRiderWeightKg(weightKg: String): Result<Unit>
}

internal class UpdateRiderWeightUseCaseImpl(
    private val repository: ProfileRepository,
) : UpdateRiderWeightUseCase {

    companion object {
        private const val MIN_WEIGHT_KG = 1.0
        private const val MAX_WEIGHT_KG = 300.0
    }

    override suspend fun updateRiderWeightKg(weightKg: String): Result<Unit> {
        val parsed = parseAndValidateWeight(weightKg)
            ?: return Result.failure(InvalidWeightException(MIN_WEIGHT_KG, MAX_WEIGHT_KG))
        repository.setRiderWeightKg(parsed)
        return Result.success(Unit)
    }

    private fun parseAndValidateWeight(weightKg: String): Double? {
        val parsed = weightKg.toDoubleOrNull() ?: return null
        return parsed.takeIf { it in MIN_WEIGHT_KG..MAX_WEIGHT_KG }
    }
}
