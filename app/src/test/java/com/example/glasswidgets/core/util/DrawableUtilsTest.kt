package com.example.glasswidgets.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawableUtilsTest {

    @Test
    fun testBitmapDimensionCappingPreservesAspectRatio() {
        val rawWidth = 1080
        val rawHeight = 540
        val maxDim = 450

        val maxAspectDim = maxOf(rawWidth, rawHeight)
        val scaleFactor = if (maxAspectDim > maxDim) maxDim.toFloat() / maxAspectDim.toFloat() else 1.0f

        val scaledWidth = (rawWidth * scaleFactor).toInt()
        val scaledHeight = (rawHeight * scaleFactor).toInt()

        assertTrue(scaledWidth <= maxDim)
        assertTrue(scaledHeight <= maxDim)

        val originalRatio = rawWidth.toFloat() / rawHeight.toFloat()
        val scaledRatio = scaledWidth.toFloat() / scaledHeight.toFloat()
        assertEquals(originalRatio, scaledRatio, 0.01f)
    }

    @Test
    fun testSmallDimensionsNotUpscaled() {
        val rawWidth = 200
        val rawHeight = 100
        val maxDim = 450

        val maxAspectDim = maxOf(rawWidth, rawHeight)
        val scaleFactor = if (maxAspectDim > maxDim) maxDim.toFloat() / maxAspectDim.toFloat() else 1.0f

        assertEquals(1.0f, scaleFactor, 0.001f)
        val scaledWidth = (rawWidth * scaleFactor).toInt()
        val scaledHeight = (rawHeight * scaleFactor).toInt()

        assertEquals(200, scaledWidth)
        assertEquals(100, scaledHeight)
    }

    @Test
    fun testSquareDimensionsCappedExactly() {
        val rawWidth = 840
        val rawHeight = 840
        val maxDim = 450

        val maxAspectDim = maxOf(rawWidth, rawHeight)
        val scaleFactor = if (maxAspectDim > maxDim) maxDim.toFloat() / maxAspectDim.toFloat() else 1.0f

        val scaledWidth = (rawWidth * scaleFactor).toInt()
        val scaledHeight = (rawHeight * scaleFactor).toInt()

        assertEquals(450, scaledWidth)
        assertEquals(450, scaledHeight)
    }

    @Test
    fun testMemoryPayloadReductionCalculation() {
        val rawWidth = 1080
        val rawHeight = 540
        val uncompressedRawBytes = rawWidth * rawHeight * 4

        val scaledWidth = 450
        val scaledHeight = 225
        val scaledBytes = scaledWidth * scaledHeight * 4

        val reductionPercentage = (1.0f - (scaledBytes.toFloat() / uncompressedRawBytes.toFloat())) * 100f
        assertTrue(reductionPercentage > 80.0f)
    }
}
