package com.koflox.concurrent

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentFactoryTest {

    companion object {
        private const val TEST_VALUE = "test-value"
        private const val CONCURRENT_COROUTINES = 100
        private const val CREATION_DELAY_MS = 50L
    }

    @Test
    fun `get returns instance created by create`() = runTest {
        val factory = createStringFactory(TEST_VALUE)

        val result = factory.get()

        assertEquals(TEST_VALUE, result)
    }

    @Test
    fun `get caches instance after first creation`() = runTest {
        val createCount = AtomicInteger(0)
        val factory = createCountingFactory(TEST_VALUE, createCount)

        val first = factory.get()
        val second = factory.get()

        assertSame(first, second)
        assertEquals(1, createCount.get())
    }

    @Test
    fun `get handles concurrent access safely`() = runTest {
        val createCount = AtomicInteger(0)
        val factory = createCountingFactory(TEST_VALUE, createCount, delayMs = CREATION_DELAY_MS)

        val results = (1..CONCURRENT_COROUTINES).map {
            async { factory.get() }
        }.awaitAll()

        val firstResult = results.first()
        results.forEach { assertSame(firstResult, it) }
        assertEquals(1, createCount.get())
    }

    @Test
    fun `get retries after creation failure`() = runTest {
        val callCount = AtomicInteger(0)
        val factory = object : ConcurrentFactory<String>() {
            override suspend fun create(): String {
                if (callCount.incrementAndGet() == 1) {
                    throw IllegalStateException("First call fails")
                }
                return TEST_VALUE
            }
        }

        val firstResult = runCatching { factory.get() }
        val secondResult = factory.get()

        assertEquals(true, firstResult.isFailure)
        assertEquals(TEST_VALUE, secondResult)
        assertEquals(2, callCount.get())
    }

    private fun createStringFactory(value: String) = object : ConcurrentFactory<String>() {
        override suspend fun create(): String = value
    }

    private fun createCountingFactory(
        value: String,
        counter: AtomicInteger,
        delayMs: Long = 0L,
    ) = object : ConcurrentFactory<String>() {
        override suspend fun create(): String {
            if (delayMs > 0) delay(delayMs)
            counter.incrementAndGet()
            return value
        }
    }
}
