package com.koflox.error.mapper

import com.koflox.designsystem.text.UiText

/**
 * Domain layer errors' mapping interface for implementations in feature modules
 */
interface ErrorMessageMapper {
    suspend fun map(error: Throwable): UiText
}
