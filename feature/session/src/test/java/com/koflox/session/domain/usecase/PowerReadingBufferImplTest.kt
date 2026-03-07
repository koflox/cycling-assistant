package com.koflox.session.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PowerReadingBufferImplTest {

    companion object {
        private const val BASE_MS = 10_000L
    }

    private val buffer = PowerReadingBufferImpl()

    @Test
    fun `drainMedian returns null for empty buffer`() {
        assertNull(buffer.drainMedian(fromMs = 0L, toMs = BASE_MS))
    }

    @Test
    fun `drainMedian returns single reading value`() {
        buffer.addReading(powerWatts = 200, timestampMs = BASE_MS)

        assertEquals(200, buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS))
    }

    @Test
    fun `drainMedian returns median of odd count`() {
        buffer.addReading(powerWatts = 100, timestampMs = BASE_MS)
        buffer.addReading(powerWatts = 300, timestampMs = BASE_MS + 1000L)
        buffer.addReading(powerWatts = 200, timestampMs = BASE_MS + 2000L)

        assertEquals(200, buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS + 2000L))
    }

    @Test
    fun `drainMedian returns upper median of even count`() {
        buffer.addReading(powerWatts = 100, timestampMs = BASE_MS)
        buffer.addReading(powerWatts = 200, timestampMs = BASE_MS + 1000L)
        buffer.addReading(powerWatts = 300, timestampMs = BASE_MS + 2000L)
        buffer.addReading(powerWatts = 400, timestampMs = BASE_MS + 3000L)

        assertEquals(300, buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS + 3000L))
    }

    @Test
    fun `drainMedian only considers readings in range`() {
        buffer.addReading(powerWatts = 999, timestampMs = BASE_MS - 1L)
        buffer.addReading(powerWatts = 150, timestampMs = BASE_MS)
        buffer.addReading(powerWatts = 200, timestampMs = BASE_MS + 1000L)
        buffer.addReading(powerWatts = 250, timestampMs = BASE_MS + 2000L)
        buffer.addReading(powerWatts = 999, timestampMs = BASE_MS + 3000L)

        assertEquals(200, buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS + 2000L))
    }

    @Test
    fun `drainMedian removes drained readings`() {
        buffer.addReading(powerWatts = 100, timestampMs = BASE_MS)
        buffer.addReading(powerWatts = 200, timestampMs = BASE_MS + 1000L)
        buffer.addReading(powerWatts = 300, timestampMs = BASE_MS + 5000L)

        buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS + 1000L)
        val second = buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS + 1000L)

        assertNull(second)
    }

    @Test
    fun `drainMedian preserves readings after toMs`() {
        buffer.addReading(powerWatts = 100, timestampMs = BASE_MS)
        buffer.addReading(powerWatts = 300, timestampMs = BASE_MS + 5000L)

        buffer.drainMedian(fromMs = BASE_MS, toMs = BASE_MS + 1000L)
        val result = buffer.drainMedian(fromMs = BASE_MS + 4000L, toMs = BASE_MS + 6000L)

        assertEquals(300, result)
    }

    @Test
    fun `clear empties the buffer`() {
        buffer.addReading(powerWatts = 200, timestampMs = BASE_MS)
        buffer.addReading(powerWatts = 300, timestampMs = BASE_MS + 1000L)

        buffer.clear()

        assertNull(buffer.drainMedian(fromMs = 0L, toMs = Long.MAX_VALUE))
    }
}
