package com.koflox.concurrent

import kotlin.coroutines.cancellation.CancellationException

/**
 * A coroutine-safe version of [runCatching] that properly handles [CancellationException].
 *
 * Unlike [runCatching], this function rethrows [CancellationException] to preserve
 * structured concurrency semantics. It also rethrows [Error] subclasses since they
 * typically indicate unrecoverable JVM errors (e.g., OutOfMemoryError).
 *
 * @param block The suspending block to execute.
 * @return [Result.success] if the block completes successfully, or [Result.failure]
 *         if it throws an [Exception] (excluding [CancellationException]).
 */
@Suppress("TooGenericExceptionCaught")
inline fun <R> suspendRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
