package com.koflox.sensor.power.presentation.testmode.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.koflox.sensor.power.presentation.testmode.PowerReadingUiModel

@Composable
internal fun PowerChart(
    readings: List<PowerReadingUiModel>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (readings.size < 2) return@Canvas
        val maxPower = readings.maxOf { it.powerWatts }.coerceAtLeast(1)
        val stepX = width / (readings.size - 1).toFloat()
        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
            )
        }
        // Draw power line
        val path = Path()
        readings.forEachIndexed { index, reading ->
            val x = index * stepX
            val y = height - (reading.powerWatts.toFloat() / maxPower * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f),
        )
    }
}
