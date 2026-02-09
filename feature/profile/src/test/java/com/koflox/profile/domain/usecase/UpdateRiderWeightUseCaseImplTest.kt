package com.koflox.profile.domain.usecase

import com.koflox.profile.domain.model.InvalidWeightException
import com.koflox.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateRiderWeightUseCaseImplTest {

    companion object {
        private const val VALID_WEIGHT = "75.0"
        private const val MIN_WEIGHT = "1.0"
        private const val MAX_WEIGHT = "300.0"
        private const val BELOW_MIN_WEIGHT = "0.5"
        private const val ABOVE_MAX_WEIGHT = "301.0"
        private const val ZERO_WEIGHT = "0"
        private const val NEGATIVE_WEIGHT = "-5.0"
        private const val NON_NUMERIC_WEIGHT = "abc"
        private const val EMPTY_WEIGHT = ""
    }

    private val repository: ProfileRepository = mockk()
    private lateinit var useCase: UpdateRiderWeightUseCaseImpl

    @Before
    fun setup() {
        coEvery { repository.setRiderWeightKg(any()) } returns Unit
        useCase = UpdateRiderWeightUseCaseImpl(repository = repository)
    }

    @Test
    fun `updateRiderWeightKg succeeds for valid weight`() = runTest {
        val result = useCase.updateRiderWeightKg(VALID_WEIGHT)

        assertTrue(result.isSuccess)
        coVerify { repository.setRiderWeightKg(VALID_WEIGHT.toDouble()) }
    }

    @Test
    fun `updateRiderWeightKg succeeds for minimum weight`() = runTest {
        val result = useCase.updateRiderWeightKg(MIN_WEIGHT)

        assertTrue(result.isSuccess)
        coVerify { repository.setRiderWeightKg(MIN_WEIGHT.toDouble()) }
    }

    @Test
    fun `updateRiderWeightKg succeeds for maximum weight`() = runTest {
        val result = useCase.updateRiderWeightKg(MAX_WEIGHT)

        assertTrue(result.isSuccess)
        coVerify { repository.setRiderWeightKg(MAX_WEIGHT.toDouble()) }
    }

    @Test
    fun `updateRiderWeightKg fails for weight below minimum`() = runTest {
        val result = useCase.updateRiderWeightKg(BELOW_MIN_WEIGHT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidWeightException)
        coVerify(exactly = 0) { repository.setRiderWeightKg(any()) }
    }

    @Test
    fun `updateRiderWeightKg fails for weight above maximum`() = runTest {
        val result = useCase.updateRiderWeightKg(ABOVE_MAX_WEIGHT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidWeightException)
        coVerify(exactly = 0) { repository.setRiderWeightKg(any()) }
    }

    @Test
    fun `updateRiderWeightKg fails for zero weight`() = runTest {
        val result = useCase.updateRiderWeightKg(ZERO_WEIGHT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidWeightException)
    }

    @Test
    fun `updateRiderWeightKg fails for negative weight`() = runTest {
        val result = useCase.updateRiderWeightKg(NEGATIVE_WEIGHT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidWeightException)
    }

    @Test
    fun `updateRiderWeightKg fails for non-numeric input`() = runTest {
        val result = useCase.updateRiderWeightKg(NON_NUMERIC_WEIGHT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidWeightException)
    }

    @Test
    fun `updateRiderWeightKg fails for empty input`() = runTest {
        val result = useCase.updateRiderWeightKg(EMPTY_WEIGHT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidWeightException)
    }
}
