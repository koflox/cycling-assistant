package com.koflox.location.usecase

import com.koflox.location.settings.LocationSettingsDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckLocationEnabledUseCaseTest {

    private val locationSettingsDataSource: LocationSettingsDataSource = mockk()
    private lateinit var useCase: CheckLocationEnabledUseCase

    @Before
    fun setup() {
        useCase = CheckLocationEnabledUseCaseImpl(
            locationSettingsDataSource = locationSettingsDataSource,
        )
    }

    @Test
    fun `isLocationEnabled delegates to data source`() {
        every { locationSettingsDataSource.isLocationEnabled() } returns true

        assertTrue(useCase.isLocationEnabled())
        verify { locationSettingsDataSource.isLocationEnabled() }
    }

    @Test
    fun `isLocationEnabled returns false when data source returns false`() {
        every { locationSettingsDataSource.isLocationEnabled() } returns false

        assertFalse(useCase.isLocationEnabled())
    }

    @Test
    fun `observeLocationEnabled delegates to data source`() = runTest {
        val flow = MutableStateFlow(true)
        every { locationSettingsDataSource.observeLocationEnabled() } returns flow

        val result = useCase.observeLocationEnabled().first()

        assertTrue(result)
        verify { locationSettingsDataSource.observeLocationEnabled() }
    }

    @Test
    fun `observeLocationEnabled emits false when data source emits false`() = runTest {
        val flow = MutableStateFlow(false)
        every { locationSettingsDataSource.observeLocationEnabled() } returns flow

        val result = useCase.observeLocationEnabled().first()

        assertFalse(result)
    }
}
