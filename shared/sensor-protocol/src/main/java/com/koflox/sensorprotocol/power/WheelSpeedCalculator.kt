package com.koflox.sensorprotocol.power

/**
 * Calculates wheel speed (km/h) from cumulative wheel revolution data
 * per the Bluetooth Cycling Power Measurement specification.
 *
 * Stateful — stores the previous sample to compute deltas between consecutive calls.
 * Create a new instance per session.
 *
 * @param wheelCircumferenceM wheel circumference in meters. Default `2.096` (700×25c).
 */
interface WheelSpeedCalculator {

    /**
     * Computes speed from two consecutive Cycling Power Measurement samples.
     *
     * @param cumulativeWheelRevolutions cumulative wheel revolutions (32-bit, wraps at 0xFFFFFFFF).
     * @param lastWheelEventTime timestamp of the last wheel event in 1/2048 second units
     *   (16-bit, wraps at 0xFFFF).
     * @return speed in km/h, or `null` when:
     *   - this is the first call (no previous sample to compare),
     *   - the wheel is stationary (delta revolutions = 0),
     *   - two samples share the same timestamp (delta time = 0).
     */
    fun calculate(cumulativeWheelRevolutions: Long, lastWheelEventTime: Int): Float?
}

fun WheelSpeedCalculator(
    wheelCircumferenceM: Float = WheelSpeedCalculatorImpl.DEFAULT_WHEEL_CIRCUMFERENCE_M,
): WheelSpeedCalculator = WheelSpeedCalculatorImpl(wheelCircumferenceM)

/**
 * Speed formula:
 * ```
 * speed_m_s = (deltaRevs * circumference) / (deltaTime / 2048)
 * speed_km_h = speed_m_s * 3.6
 * ```
 * where `deltaTime` is in 1/2048s units.
 *
 * Wheel revolutions are 32-bit and wrap at 0xFFFFFFFF.
 * Event time is 16-bit and wraps at 0xFFFF.
 */
internal class WheelSpeedCalculatorImpl(
    private val wheelCircumferenceM: Float = DEFAULT_WHEEL_CIRCUMFERENCE_M,
) : WheelSpeedCalculator {

    companion object {
        const val DEFAULT_WHEEL_CIRCUMFERENCE_M = 2.096f
        private const val WHEEL_EVENT_TIME_RESOLUTION = 2048f
        private const val MS_TO_KMH = 3.6f
        private const val MAX_WHEEL_REVOLUTIONS = 0xFFFFFFFFL
        private const val MAX_WHEEL_EVENT_TIME = 0xFFFF
    }

    private var previousWheelRevolutions: Long? = null
    private var previousWheelEventTime: Int? = null

    override fun calculate(cumulativeWheelRevolutions: Long, lastWheelEventTime: Int): Float? {
        val prevRevs = previousWheelRevolutions
        val prevTime = previousWheelEventTime
        previousWheelRevolutions = cumulativeWheelRevolutions
        previousWheelEventTime = lastWheelEventTime
        if (prevRevs == null || prevTime == null) return null
        val deltaRevs = (cumulativeWheelRevolutions - prevRevs) and MAX_WHEEL_REVOLUTIONS
        val deltaTime = (lastWheelEventTime - prevTime) and MAX_WHEEL_EVENT_TIME
        return if (deltaTime == 0 || deltaRevs == 0L) {
            null
        } else {
            val speedMs = deltaRevs.toFloat() * wheelCircumferenceM / (deltaTime.toFloat() / WHEEL_EVENT_TIME_RESOLUTION)
            speedMs * MS_TO_KMH
        }
    }
}
