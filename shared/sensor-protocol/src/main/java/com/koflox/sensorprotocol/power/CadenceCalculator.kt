package com.koflox.sensorprotocol.power

/**
 * Calculates cadence (RPM) from cumulative crank revolution data
 * per the Bluetooth Cycling Power Measurement specification.
 *
 * Stateful — stores the previous sample to compute deltas between consecutive calls.
 * Create a new instance per session.
 */
interface CadenceCalculator {

    /**
     * Computes cadence from two consecutive Cycling Power Measurement samples.
     *
     * @param crankRevolutions cumulative crank revolutions (16-bit, wraps at 0xFFFF).
     * @param lastCrankEventTime timestamp of the last crank event in 1/1024 second units
     *   (16-bit, wraps at 0xFFFF).
     * @return cadence in RPM, or `null` when:
     *   - this is the first call (no previous sample to compare),
     *   - the crank is stationary (delta revolutions = 0),
     *   - two samples share the same timestamp (delta time = 0).
     */
    fun calculate(crankRevolutions: Int, lastCrankEventTime: Int): Float?
}

fun CadenceCalculator(): CadenceCalculator = CadenceCalculatorImpl()

/**
 * Cadence formula:
 * ```
 * RPM = (deltaRevs / deltaTime) * 60 * 1024
 * ```
 * where `deltaTime` is in 1/1024s units — multiplying by 1024 converts to seconds,
 * and multiplying by 60 converts to minutes.
 *
 * Both counters are 16-bit and wrap around at 0xFFFF.
 * Bitwise `and 0xFFFF` handles the wrap-around correctly
 * (e.g., current = 2, previous = 0xFFFE → delta = 4).
 */
internal class CadenceCalculatorImpl : CadenceCalculator {

    companion object {
        private const val CRANK_EVENT_TIME_RESOLUTION = 1024f
        private const val SECONDS_PER_MINUTE = 60f
        private const val MAX_CRANK_EVENT_TIME = 0xFFFF
    }

    private var previousCrankRevolutions: Int? = null
    private var previousCrankEventTime: Int? = null

    override fun calculate(crankRevolutions: Int, lastCrankEventTime: Int): Float? {
        val prevRevs = previousCrankRevolutions
        val prevTime = previousCrankEventTime
        previousCrankRevolutions = crankRevolutions
        previousCrankEventTime = lastCrankEventTime
        if (prevRevs == null || prevTime == null) return null
        val deltaRevs = (crankRevolutions - prevRevs) and MAX_CRANK_EVENT_TIME
        val deltaTime = (lastCrankEventTime - prevTime) and MAX_CRANK_EVENT_TIME
        return if (deltaTime == 0 || deltaRevs == 0) {
            null
        } else {
            deltaRevs.toFloat() / deltaTime.toFloat() * SECONDS_PER_MINUTE * CRANK_EVENT_TIME_RESOLUTION
        }
    }
}
