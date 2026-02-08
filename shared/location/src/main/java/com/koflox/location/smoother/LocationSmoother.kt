package com.koflox.location.smoother

import com.koflox.location.model.Location
import kotlin.math.cos

interface LocationSmoother {
    fun smooth(location: Location, timestampMs: Long): Location
    fun reset()
}

/**
 * 1D Kalman filter applied independently to latitude (Y) and longitude (X).
 *
 * Coordinates are converted to a local metric frame (meters from the first observed point)
 * so that the filter operates in meters and the Kalman gain has physical meaning.
 *
 * **State model (per axis):**
 * - State: position in meters relative to [referenceLatitude]/[referenceLongitude].
 * - Prediction: position is assumed constant (no velocity model);
 *   uncertainty grows over time by [PROCESS_NOISE_M2_PER_S] * dt.
 * - Measurement noise: GPS-reported accuracy squared (variance in m^2).
 *
 * **Kalman gain** `K = P_predicted / (P_predicted + R)` blends the prediction with
 * the new measurement: high-accuracy fixes pull the estimate strongly, while noisy
 * fixes are largely ignored.
 *
 * **Example — stationary device, Y-axis only for clarity (interval = 3 s):**
 * ```
 * #1  lat=50.00000  acc=10 m  → returned as-is (init, variance P = 100 m²)
 *
 * #2  lat=50.00010  acc=10 m  (+11 m GPS jitter)
 *     P_pred = 100 + 3·3 = 109 m²          // prediction: variance grows by process noise
 *     R      = 10² = 100 m²                 // measurement noise from GPS accuracy
 *     K      = 109 / (109 + 100) = 0.52     // roughly equal trust → splits the difference
 *     y      = 0 + 0.52 · 11.1 = 5.8 m      // filtered position (meters from ref)
 *     P      = (1 − 0.52) · 109 = 52 m²     // uncertainty shrinks after update
 *     → smoothed lat ≈ 50.00005
 *
 * #3  lat=50.00050  acc=50 m  (+55 m noisy spike)
 *     P_pred = 52 + 9 = 61 m²
 *     R      = 50² = 2500 m²                // very noisy measurement
 *     K      = 61 / (61 + 2500) = 0.02      // filter nearly ignores this fix
 *     y      = 5.8 + 0.02 · (55.7 − 5.8) = 6.8 m
 *     → smoothed lat ≈ 50.00006             // spike dampened from +55 m to +1 m shift
 *
 * #4  lat=50.00006  acc=5 m   (accurate fix after noisy one)
 *     P_pred = 60 + 9 = 69 m²
 *     R      = 5² = 25 m²                   // high-accuracy measurement
 *     K      = 69 / (69 + 25) = 0.73        // filter trusts this fix strongly
 *     y      = 6.8 + 0.73 · (6.7 − 6.8) = 6.7 m
 *     → smoothed lat ≈ 50.00006             // converges quickly on accurate data
 * ```
 *
 * Locations without [Location.accuracyMeters] are returned unchanged (nothing to filter on).
 * The filter is stateful and must be used within a single session; call [reset] between sessions.
 */
internal class KalmanLocationSmoother : LocationSmoother {

    companion object {
        /** How fast uncertainty grows between measurements (m^2/s). */
        private const val PROCESS_NOISE_M2_PER_S = 3.0
        private const val METERS_PER_DEGREE_LAT = 111_320.0
        private const val MILLISECONDS_PER_SECOND = 1000.0
    }

    private var isInitialized: Boolean = false

    /** Origin of the local metric frame, set on the first measurement. */
    private var referenceLatitude: Double = 0.0
    private var referenceLongitude: Double = 0.0
    private var metersPerDegreeLon: Double = 0.0
    private var lastTimestampMs: Long = 0L

    /** Filtered position (meters from reference) and its variance per axis. */
    private var xMeters: Double = 0.0
    private var xVariance: Double = 0.0
    private var yMeters: Double = 0.0
    private var yVariance: Double = 0.0

    override fun smooth(location: Location, timestampMs: Long): Location {
        val accuracy = location.accuracyMeters ?: return location
        return if (isInitialized) update(location, accuracy, timestampMs) else initialize(location, accuracy, timestampMs)
    }

    override fun reset() {
        isInitialized = false
        referenceLatitude = 0.0
        referenceLongitude = 0.0
        metersPerDegreeLon = 0.0
        lastTimestampMs = 0L
        xMeters = 0.0
        xVariance = 0.0
        yMeters = 0.0
        yVariance = 0.0
    }

    /**
     * Seeds the filter with the first observation.
     * The reference point is set here; initial variance comes from GPS accuracy.
     */
    private fun initialize(
        location: Location,
        accuracy: Float,
        timestampMs: Long,
    ): Location {
        referenceLatitude = location.latitude
        referenceLongitude = location.longitude
        metersPerDegreeLon = METERS_PER_DEGREE_LAT * cos(Math.toRadians(referenceLatitude))
        lastTimestampMs = timestampMs
        xMeters = 0.0
        yMeters = 0.0
        val variance = (accuracy * accuracy).toDouble()
        xVariance = variance
        yVariance = variance
        isInitialized = true
        return location
    }

    /**
     * Predict-update cycle:
     * 1. Convert new GPS fix to meters from the reference point.
     * 2. **Predict**: grow variance by process noise proportional to elapsed time.
     * 3. **Update**: compute Kalman gain, blend estimate with measurement,
     *    shrink variance.
     * 4. Convert filtered position back to lat/lon.
     */
    private fun update(
        location: Location,
        accuracy: Float,
        timestampMs: Long,
    ): Location {
        val measuredXMeters = (location.longitude - referenceLongitude) * metersPerDegreeLon
        val measuredYMeters = (location.latitude - referenceLatitude) * METERS_PER_DEGREE_LAT

        val dtSeconds = (timestampMs - lastTimestampMs).coerceAtLeast(1L) / MILLISECONDS_PER_SECOND
        val processNoise = PROCESS_NOISE_M2_PER_S * dtSeconds
        val measurementNoise = (accuracy * accuracy).toDouble()

        // Predict
        val xPredictedVariance = xVariance + processNoise
        val yPredictedVariance = yVariance + processNoise

        // Update
        val kx = xPredictedVariance / (xPredictedVariance + measurementNoise)
        val ky = yPredictedVariance / (yPredictedVariance + measurementNoise)

        xMeters += kx * (measuredXMeters - xMeters)
        yMeters += ky * (measuredYMeters - yMeters)
        xVariance = (1.0 - kx) * xPredictedVariance
        yVariance = (1.0 - ky) * yPredictedVariance
        lastTimestampMs = timestampMs

        val smoothedLatitude = referenceLatitude + yMeters / METERS_PER_DEGREE_LAT
        val smoothedLongitude = referenceLongitude + xMeters / metersPerDegreeLon

        return location.copy(
            latitude = smoothedLatitude,
            longitude = smoothedLongitude,
        )
    }
}
