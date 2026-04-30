package com.koflox.session.presentation.route

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.koflox.map.RouteColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteColorStrategyTest {

    companion object {
        private val NORMAL_COLOR = RouteColors.NormalSpeed.toArgb()
        private val FAST_COLOR = RouteColors.FastSpeed.toArgb()
        private val LOW_POWER_COLOR = RouteColors.LowPower.toArgb()
        private val MEDIUM_POWER_COLOR = RouteColors.MediumPower.toArgb()
        private val HIGH_POWER_COLOR = RouteColors.HighPower.toArgb()
    }

    @Test
    fun `DefaultRouteColorStrategy returns uniform color for all points`() {
        val points = listOf(
            createRoutePoint(speedKmh = 10.0),
            createRoutePoint(speedKmh = 50.0),
            createRoutePoint(speedKmh = 25.0),
        )
        val colors = DefaultRouteColorStrategy().assignColors(points)
        assertEquals(3, colors.size)
        assertTrue(colors.all { it == NORMAL_COLOR })
    }

    @Test
    fun `SpeedRouteColorStrategy maps normal speed to normal color`() {
        val points = List(5) { createRoutePoint(speedKmh = 20.0) }
        val colors = SpeedRouteColorStrategy().assignColors(points)
        assertTrue(colors.all { it == NORMAL_COLOR })
    }

    @Test
    fun `SpeedRouteColorStrategy maps fast speed to fast color`() {
        val points = List(5) { createRoutePoint(speedKmh = 35.0) }
        val colors = SpeedRouteColorStrategy().assignColors(points)
        assertTrue(colors.all { it == FAST_COLOR })
    }

    @Test
    fun `SpeedRouteColorStrategy applies smoothing window`() {
        val points = List(5) { createRoutePoint(speedKmh = 20.0) } +
            listOf(createRoutePoint(speedKmh = 40.0)) +
            List(5) { createRoutePoint(speedKmh = 20.0) }
        val colors = SpeedRouteColorStrategy().assignColors(points)
        assertEquals(NORMAL_COLOR, colors[0])
        assertEquals(NORMAL_COLOR, colors.last())
    }

    @Test
    fun `PowerRouteColorStrategy maps low power zone`() {
        val points = List(5) { createRoutePoint(powerWatts = 100) }
        val colors = PowerRouteColorStrategy().assignColors(points)
        assertTrue(colors.all { it == LOW_POWER_COLOR })
    }

    @Test
    fun `PowerRouteColorStrategy maps medium power zone`() {
        val points = List(5) { createRoutePoint(powerWatts = 200) }
        val colors = PowerRouteColorStrategy().assignColors(points)
        assertTrue(colors.all { it == MEDIUM_POWER_COLOR })
    }

    @Test
    fun `PowerRouteColorStrategy maps high power zone`() {
        val points = List(5) { createRoutePoint(powerWatts = 300) }
        val colors = PowerRouteColorStrategy().assignColors(points)
        assertTrue(colors.all { it == HIGH_POWER_COLOR })
    }

    @Test
    fun `PowerRouteColorStrategy treats null power as low zone`() {
        val points = List(5) { createRoutePoint(powerWatts = null) }
        val colors = PowerRouteColorStrategy().assignColors(points)
        assertTrue(colors.all { it == LOW_POWER_COLOR })
    }

    @Test
    fun `PowerRouteColorStrategy applies smoothing window`() {
        val points = List(5) { createRoutePoint(powerWatts = 100) } +
            listOf(createRoutePoint(powerWatts = 300)) +
            List(5) { createRoutePoint(powerWatts = 100) }
        val colors = PowerRouteColorStrategy().assignColors(points)
        assertEquals(LOW_POWER_COLOR, colors[0])
        assertEquals(LOW_POWER_COLOR, colors.last())
    }

    private fun createRoutePoint(
        speedKmh: Double = 0.0,
        powerWatts: Int? = null,
    ) = RoutePoint(
        latLng = LatLng(0.0, 0.0),
        speedKmh = speedKmh,
        powerWatts = powerWatts,
    )
}
