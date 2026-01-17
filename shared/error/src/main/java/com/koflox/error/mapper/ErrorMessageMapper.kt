package com.koflox.error.mapper

/**
 * Domain layer errors' mapping interface for implementations in feature modules
 */
interface ErrorMessageMapper {
    suspend fun map(error: Throwable): String
}
