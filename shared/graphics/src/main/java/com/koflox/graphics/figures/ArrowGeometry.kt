package com.koflox.graphics.figures

object ArrowShape {
    const val LENGTH_RATIO = 0.8f
    const val SPREAD_RATIO = 0.6f
}

data class ArrowVertices(
    val tipX: Float,
    val tipY: Float,
    val baseUpperX: Float,
    val baseUpperY: Float,
    val baseLowerX: Float,
    val baseLowerY: Float,
)

fun computeArrowVertices(size: Float): ArrowVertices {
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
