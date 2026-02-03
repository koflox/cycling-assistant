package com.koflox.profile.data.repository

import com.koflox.profile.data.source.ProfileLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProfileRepositoryImplTest {

    companion object {
        private const val RIDER_WEIGHT_KG = 75.0f
        private const val NEW_WEIGHT_KG = 80.0
    }

    private val localDataSource: ProfileLocalDataSource = mockk(relaxUnitFun = true)
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setup() {
        repository = ProfileRepositoryImpl(localDataSource)
    }

    @Test
    fun `getRiderWeightKg delegates to data source`() = runTest {
        coEvery { localDataSource.getRiderWeightKg() } returns RIDER_WEIGHT_KG

        val result = repository.getRiderWeightKg()

        assertEquals(RIDER_WEIGHT_KG, result)
    }

    @Test
    fun `getRiderWeightKg returns null when not set`() = runTest {
        coEvery { localDataSource.getRiderWeightKg() } returns null

        val result = repository.getRiderWeightKg()

        assertNull(result)
    }

    @Test
    fun `setRiderWeightKg delegates to data source`() = runTest {
        repository.setRiderWeightKg(NEW_WEIGHT_KG)

        coVerify { localDataSource.setRiderWeightKg(NEW_WEIGHT_KG) }
    }
}
