package com.koflox.graphics.figures

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrowGeometryTest {

    companion object {
        private const val DELTA = 0.001f
        private const val SIZE = 100f
    }

    @Test
    fun `tip is to the right of center`() {
        val vertices = computeArrowVertices(SIZE)
        assertTrue(vertices.tipX > SIZE / 2f)
    }

    @Test
    fun `tip is vertically centered`() {
        val vertices = computeArrowVertices(SIZE)
        assertEquals(SIZE / 2f, vertices.tipY, DELTA)
    }

    @Test
    fun `base points are to the left of center`() {
        val vertices = computeArrowVertices(SIZE)
        assertTrue(vertices.baseUpperX < SIZE / 2f)
        assertTrue(vertices.baseLowerX < SIZE / 2f)
    }

    @Test
    fun `base points are vertically symmetric`() {
        val vertices = computeArrowVertices(SIZE)
        assertEquals(vertices.baseUpperX, vertices.baseLowerX, DELTA)
        val center = SIZE / 2f
        assertEquals(center - vertices.baseUpperY, vertices.baseLowerY - center, DELTA)
    }

    @Test
    fun `tip x matches length ratio`() {
        val vertices = computeArrowVertices(SIZE)
        val expectedTipX = SIZE / 2f + SIZE * ArrowShape.LENGTH_RATIO / 2f
        assertEquals(expectedTipX, vertices.tipX, DELTA)
    }

    @Test
    fun `spread matches spread ratio`() {
        val vertices = computeArrowVertices(SIZE)
        val spread = vertices.baseLowerY - vertices.baseUpperY
        assertEquals(SIZE * ArrowShape.SPREAD_RATIO, spread, DELTA)
    }

    @Test
    fun `zero size produces all zero vertices`() {
        val vertices = computeArrowVertices(0f)
        assertEquals(0f, vertices.tipX, DELTA)
        assertEquals(0f, vertices.tipY, DELTA)
        assertEquals(0f, vertices.baseUpperX, DELTA)
        assertEquals(0f, vertices.baseUpperY, DELTA)
        assertEquals(0f, vertices.baseLowerX, DELTA)
        assertEquals(0f, vertices.baseLowerY, DELTA)
    }
}
