package com.koflox.poi.domain.usecase

import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.repository.PoiRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class UpdateSelectedPoisUseCaseImplTest {

    private val repository: PoiRepository = mockk()
    private lateinit var useCase: UpdateSelectedPoisUseCaseImpl

    @Before
    fun setup() {
        coEvery { repository.updateSelectedPois(any()) } returns Unit
        useCase = UpdateSelectedPoisUseCaseImpl(repository = repository)
    }

    @Test
    fun `valid selection of 2 updates repository`() = runTest {
        val pois = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
        useCase.updateSelectedPois(pois)
        coVerify { repository.updateSelectedPois(pois) }
    }

    @Test
    fun `selection of 1 throws InvalidPoiSelectionException`() = runTest {
        val pois = listOf(PoiType.COFFEE_SHOP)
        try {
            useCase.updateSelectedPois(pois)
            fail("Expected InvalidPoiSelectionException")
        } catch (e: InvalidPoiSelectionException) {
            assertTrue(e.message!!.contains("2"))
        }
    }

    @Test
    fun `selection of 3 throws InvalidPoiSelectionException`() = runTest {
        val pois = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET, PoiType.PARK)
        try {
            useCase.updateSelectedPois(pois)
            fail("Expected InvalidPoiSelectionException")
        } catch (e: InvalidPoiSelectionException) {
            assertTrue(e.message!!.contains("2"))
        }
    }
}
