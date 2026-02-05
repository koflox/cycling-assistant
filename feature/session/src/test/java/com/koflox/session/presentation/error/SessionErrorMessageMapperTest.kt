package com.koflox.session.presentation.error

import android.content.Context
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.error.LocationUnavailableException
import com.koflox.session.R
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionErrorMessageMapperTest {

    companion object {
        private const val LOCATION_UNAVAILABLE_MESSAGE = "Location unavailable"
        private const val NO_ACTIVE_SESSION_MESSAGE = "No active session"
        private const val DEFAULT_ERROR_MESSAGE = "Something went wrong"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = mockk()
    private val defaultMapper: ErrorMessageMapper = mockk()
    private lateinit var mapper: SessionErrorMessageMapper

    @Before
    fun setup() {
        every { context.getString(R.string.error_location_unavailable) } returns LOCATION_UNAVAILABLE_MESSAGE
        every { context.getString(R.string.error_no_active_session) } returns NO_ACTIVE_SESSION_MESSAGE
        mapper = SessionErrorMessageMapper(
            context = context,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            defaultMapper = defaultMapper,
        )
    }

    @Test
    fun `maps LocationUnavailableException to location error message`() = runTest {
        val result = mapper.map(LocationUnavailableException())

        assertEquals(LOCATION_UNAVAILABLE_MESSAGE, result)
    }

    @Test
    fun `maps NoActiveSessionException to no active session message`() = runTest {
        val result = mapper.map(NoActiveSessionException())

        assertEquals(NO_ACTIVE_SESSION_MESSAGE, result)
    }

    @Test
    fun `delegates unknown exceptions to default mapper`() = runTest {
        val error = RuntimeException("unknown")
        coEvery { defaultMapper.map(error) } returns DEFAULT_ERROR_MESSAGE

        val result = mapper.map(error)

        assertEquals(DEFAULT_ERROR_MESSAGE, result)
    }
}
