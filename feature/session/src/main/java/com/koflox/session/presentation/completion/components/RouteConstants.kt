package com.koflox.session.presentation.completion.components

import androidx.compose.ui.graphics.Color

internal object RouteColors {
    val NormalSpeed = Color(0xFF42A5F5)
    val FastSpeed = Color(0xFF7C4DFF)
    val Gap = Color(0xFFBDBDBD)
    val StartMarker = Color(0xFF5A6BD5)
    val EndMarker = Color(0xFFE84940)
}

internal const val ROUTE_WIDTH = 10f

internal object ArrowShape {
    const val LENGTH_RATIO = 0.8f
    const val SPREAD_RATIO = 0.6f
}

internal data class ArrowVertices(
    val tipX: Float,
    val tipY: Float,
    val baseUpperX: Float,
    val baseUpperY: Float,
    val baseLowerX: Float,
    val baseLowerY: Float,
)

internal fun computeArrowVertices(size: Float): ArrowVertices {
    val center = size / 2f
    val length = size * ArrowShape.LENGTH_RATIO
    val spread = size * ArrowShape.SPREAD_RATIO
    return ArrowVertices(
        tipX = center + length / 2f,
        tipY = center,
        baseUpperX = center - length / 2f,
        baseUpperY = center - spread / 2f,
        baseLowerX = center - length / 2f,
        baseLowerY = center + spread / 2f,
    )
}
