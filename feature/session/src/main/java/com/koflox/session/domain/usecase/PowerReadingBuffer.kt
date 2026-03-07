package com.koflox.session.domain.usecase

internal interface PowerReadingBuffer {
    fun addReading(powerWatts: Int, timestampMs: Long)
    fun drainMedian(fromMs: Long, toMs: Long): Int?
    fun clear()
}

internal class PowerReadingBufferImpl : PowerReadingBuffer {

    private val readings = ArrayDeque<TimestampedReading>()

    override fun addReading(powerWatts: Int, timestampMs: Long) {
        readings.addLast(TimestampedReading(timestampMs, powerWatts))
    }

    override fun drainMedian(fromMs: Long, toMs: Long): Int? {
        val inRange = readings.filter { it.timestampMs in fromMs..toMs }
        while (readings.isNotEmpty() && readings.first().timestampMs <= toMs) {
            readings.removeFirst()
        }
        if (inRange.isEmpty()) return null
        val sorted = inRange.map(TimestampedReading::powerWatts).sorted()
        return sorted[sorted.size / 2]
    }

    override fun clear() {
        readings.clear()
    }

    private data class TimestampedReading(val timestampMs: Long, val powerWatts: Int)
}
