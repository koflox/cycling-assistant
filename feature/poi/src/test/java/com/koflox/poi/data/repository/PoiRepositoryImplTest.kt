package com.koflox.poi.data.repository

import app.cash.turbine.test
import com.koflox.poi.data.source.PoiLocalDataSource
import com.koflox.poi.domain.model.PoiType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PoiRepositoryImplTest {

    companion object {
        private val DEFAULT_POIS = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
    }

    private val localDataSource: PoiLocalDataSource = mockk()
    private lateinit var repository: PoiRepositoryImpl

    @Before
    fun setup() {
        every { localDataSource.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        coEvery { localDataSource.updateSelectedPois(any()) } returns Unit
        repository = PoiRepositoryImpl(localDataSource = localDataSource)
    }

    @Test
    fun `observeSelectedPois delegates to data source`() = runTest {
        repository.observeSelectedPois().test {
            assertEquals(DEFAULT_POIS, awaitItem())
            awaitComplete()
        }
        verify { localDataSource.observeSelectedPois() }
    }

    @Test
    fun `updateSelectedPois delegates to data source`() = runTest {
        val pois = listOf(PoiType.PARK, PoiType.PHARMACY)
        repository.updateSelectedPois(pois)
        coVerify { localDataSource.updateSelectedPois(pois) }
    }
}
