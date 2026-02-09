package com.koflox.error.mapper

import android.content.Context
import com.koflox.error.R
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DefaultErrorMessageMapperTest {

    companion object {
        private const val ERROR_MESSAGE = "An error occurred"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = mockk()
    private lateinit var mapper: DefaultErrorMessageMapper

    @Before
    fun setup() {
        every { context.getString(R.string.error_not_handled) } returns ERROR_MESSAGE
        mapper = DefaultErrorMessageMapper(
            context = context,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `map returns error_not_handled string`() = runTest {
        val result = mapper.map(RuntimeException("test"))

        assertEquals(ERROR_MESSAGE, result)
    }

    @Test
    fun `map uses context getString with correct resource`() = runTest {
        mapper.map(IllegalArgumentException("test"))

        verify { context.getString(R.string.error_not_handled) }
    }
}
