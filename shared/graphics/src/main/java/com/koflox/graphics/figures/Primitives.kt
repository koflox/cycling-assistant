package com.koflox.graphics.figures

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap

fun createCircleBitmap(
    sizeDp: Int,
    strokeWidthDp: Int,
    @ColorInt fillColor: Int,
    @ColorInt strokeColor: Int,
    density: Float,
): Bitmap {
    val sizePx = (sizeDp * density).toInt()
    val strokeWidthPx = strokeWidthDp * density
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val center = sizePx / 2f
    val radius = (sizePx - strokeWidthPx) / 2f

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidthPx
    }

    canvas.drawCircle(center, center, radius, fillPaint)
    canvas.drawCircle(center, center, radius, strokePaint)
    return bitmap
}
