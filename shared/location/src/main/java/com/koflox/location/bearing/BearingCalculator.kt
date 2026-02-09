package com.koflox.location.bearing

import com.koflox.location.model.Location
import kotlin.math.atan2

/**
 * Calculates the rotation angle (in degrees) between two locations
 * for use with canvas-drawn markers pointing right at 0°.
 *
 * Returns clockwise degrees suitable for [android.graphics.Canvas.withRotation].
 */
fun calculateBearingDegrees(from: Location, to: Location): Float {
    val deltaLat = to.latitude - from.latitude
    val deltaLon = to.longitude - from.longitude
    val angleRadians = atan2(deltaLat, deltaLon)
    val angleDegrees = Math.toDegrees(angleRadians)
    // Canvas rotation is clockwise, atan2 is counter-clockwise — negate
    return (-angleDegrees).toFloat()
}
