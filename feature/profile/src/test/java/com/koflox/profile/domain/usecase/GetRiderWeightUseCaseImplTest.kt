package com.koflox.profile.domain.usecase

import com.koflox.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetRiderWeightUseCaseImplTest {

    companion object {
        private const val RIDER_WEIGHT_KG = 75.0f
    }

    private val repository: ProfileRepository = mockk()
    private lateinit var useCase: GetRiderWeightUseCaseImpl

    @Before
    fun setup() {
        useCase = GetRiderWeightUseCaseImpl(repository = repository)
    }

    @Test
    fun `getRiderWeightKg returns weight from repository`() = runTest {
        coEvery { repository.getRiderWeightKg() } returns RIDER_WEIGHT_KG

        val result = useCase.getRiderWeightKg()

        assertEquals(RIDER_WEIGHT_KG, result)
        coVerify { repository.getRiderWeightKg() }
    }

    @Test
    fun `getRiderWeightKg returns null when repository returns null`() = runTest {
        coEvery { repository.getRiderWeightKg() } returns null

        val result = useCase.getRiderWeightKg()

        assertNull(result)
    }
}
