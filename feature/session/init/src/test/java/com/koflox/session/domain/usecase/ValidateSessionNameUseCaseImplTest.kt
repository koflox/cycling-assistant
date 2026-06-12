package com.koflox.session.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ValidateSessionNameUseCaseImplTest {

    companion object {
        private const val CURRENT_NAME = "Morning ride"
    }

    private lateinit var useCase: ValidateSessionNameUseCaseImpl

    @Before
    fun setup() {
        useCase = ValidateSessionNameUseCaseImpl()
    }

    @Test
    fun `valid input returns Valid`() {
        val result = useCase.validate(input = "Evening ride", currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.Valid, result)
    }

    @Test
    fun `input shorter than min length returns TooShort`() {
        val result = useCase.validate(input = "ab", currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.TooShort, result)
    }

    @Test
    fun `input at min length boundary returns Valid`() {
        val result = useCase.validate(input = "abc", currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.Valid, result)
    }

    @Test
    fun `input at max length boundary returns Valid`() {
        val result = useCase.validate(input = "a".repeat(20), currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.Valid, result)
    }

    @Test
    fun `input longer than max length returns TooLong`() {
        val result = useCase.validate(input = "a".repeat(21), currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.TooLong, result)
    }

    @Test
    fun `input equal to current returns SameAsCurrent`() {
        val result = useCase.validate(input = CURRENT_NAME, currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.SameAsCurrent, result)
    }

    @Test
    fun `length is checked against trimmed input`() {
        val result = useCase.validate(input = "  ab  ", currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.TooShort, result)
    }

    @Test
    fun `equality is checked against trimmed input on both sides`() {
        val result = useCase.validate(input = "  $CURRENT_NAME  ", currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.SameAsCurrent, result)
    }

    @Test
    fun `whitespace-only input is treated as TooShort`() {
        val result = useCase.validate(input = "     ", currentName = CURRENT_NAME)

        assertEquals(SessionNameValidation.TooShort, result)
    }

    @Test
    fun `length validation takes precedence over equality`() {
        val result = useCase.validate(input = "ab", currentName = "ab")

        assertEquals(SessionNameValidation.TooShort, result)
    }
}
