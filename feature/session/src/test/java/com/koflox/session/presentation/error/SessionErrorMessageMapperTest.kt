package com.koflox.session.presentation.error

import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.error.LocationUnavailableException
import com.koflox.session.R
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionErrorMessageMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val defaultMapper: ErrorMessageMapper = mockk()
    private lateinit var mapper: SessionErrorMessageMapper

    @Before
    fun setup() {
        mapper = SessionErrorMessageMapper(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            defaultMapper = defaultMapper,
        )
    }

    @Test
    fun `maps LocationUnavailableException to location error UiText`() = runTest {
        val result = mapper.map(LocationUnavailableException())

        assertEquals(UiText.Resource(R.string.error_location_unavailable), result)
    }

    @Test
    fun `maps NoActiveSessionException to no active session UiText`() = runTest {
        val result = mapper.map(NoActiveSessionException())

        assertEquals(UiText.Resource(R.string.error_no_active_session), result)
    }

    @Test
    fun `delegates unknown exceptions to default mapper`() = runTest {
        val error = RuntimeException("unknown")
        val defaultUiText = UiText.Resource(com.koflox.error.R.string.error_not_handled)
        coEvery { defaultMapper.map(error) } returns defaultUiText

        val result = mapper.map(error)

        assertEquals(defaultUiText, result)
    }
}
