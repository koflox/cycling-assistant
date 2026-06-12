package com.koflox.session.domain.usecase

interface ValidateSessionNameUseCase {
    fun validate(input: String, currentName: String): SessionNameValidation
}

sealed interface SessionNameValidation {
    data object Valid : SessionNameValidation
    data object TooShort : SessionNameValidation
    data object TooLong : SessionNameValidation
    data object SameAsCurrent : SessionNameValidation
}

class ValidateSessionNameUseCaseImpl : ValidateSessionNameUseCase {

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 20
    }

    override fun validate(input: String, currentName: String): SessionNameValidation {
        val trimmedInput = input.trim()
        return when {
            trimmedInput.length < MIN_NAME_LENGTH -> SessionNameValidation.TooShort
            trimmedInput.length > MAX_NAME_LENGTH -> SessionNameValidation.TooLong
            trimmedInput == currentName.trim() -> SessionNameValidation.SameAsCurrent
            else -> SessionNameValidation.Valid
        }
    }
}
