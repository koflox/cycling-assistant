package com.koflox.poi.domain.usecase

import app.cash.turbine.test
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.repository.PoiRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveSelectedPoisUseCaseImplTest {

    companion object {
        private val DEFAULT_POIS = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
    }

    private val repository: PoiRepository = mockk()
    private lateinit var useCase: ObserveSelectedPoisUseCaseImpl

    @Before
    fun setup() {
        every { repository.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        useCase = ObserveSelectedPoisUseCaseImpl(repository = repository)
    }

    @Test
    fun `observeSelectedPois delegates to repository`() = runTest {
        useCase.observeSelectedPois().test {
            assertEquals(DEFAULT_POIS, awaitItem())
            awaitComplete()
        }
        verify { repository.observeSelectedPois() }
    }
}
