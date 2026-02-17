package com.koflox.error.mapper

import com.koflox.designsystem.text.UiText
import com.koflox.error.R
import com.koflox.testing.coroutine.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DefaultErrorMessageMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mapper: DefaultErrorMessageMapper

    @Before
    fun setup() {
        mapper = DefaultErrorMessageMapper(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `map returns UiText Resource with error_not_handled`() = runTest {
        val result = mapper.map(RuntimeException("test"))

        assertEquals(UiText.Resource(R.string.error_not_handled), result)
    }
}
