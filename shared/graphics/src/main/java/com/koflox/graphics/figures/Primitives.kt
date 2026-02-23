package com.koflox.graphics.figures

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation

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

fun createFilledCircleBitmap(
    sizePx: Int,
    @ColorInt color: Int,
): Bitmap {
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val center = sizePx / 2f
    canvas.drawCircle(center, center, center, paint)
    return bitmap
}

fun createArrowBitmap(
    sizePx: Int,
    @ColorInt color: Int,
    rotationDegrees: Float,
): Bitmap {
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val vertices = computeArrowVertices(sizePx.toFloat())
    canvas.withRotation(rotationDegrees, sizePx / 2f, sizePx / 2f) {
        val path = Path().apply {
            moveTo(vertices.tipX, vertices.tipY)
            lineTo(vertices.baseUpperX, vertices.baseUpperY)
            lineTo(vertices.baseLowerX, vertices.baseLowerY)
            close()
        }
        drawPath(path, paint)
    }
    return bitmap
}

fun createArrowBitmap(
    sizeDp: Int,
    strokeWidthDp: Int,
    @ColorInt fillColor: Int,
    @ColorInt strokeColor: Int,
    density: Float,
    rotationDegrees: Float,
): Bitmap {
    val sizePx = (sizeDp * density).toInt()
    val strokeWidthPx = strokeWidthDp * density
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val vertices = computeArrowVertices(sizePx.toFloat())
    val arrowPath = Path().apply {
        moveTo(vertices.tipX, vertices.tipY)
        lineTo(vertices.baseUpperX, vertices.baseUpperY)
        lineTo(vertices.baseLowerX, vertices.baseLowerY)
        close()
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidthPx
        strokeJoin = Paint.Join.ROUND
    }
    canvas.withRotation(rotationDegrees, sizePx / 2f, sizePx / 2f) {
        drawPath(arrowPath, fillPaint)
        drawPath(arrowPath, strokePaint)
    }
    return bitmap
}
