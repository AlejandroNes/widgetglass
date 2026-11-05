package com.example.glasswidgets.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

object DrawableUtils {

    /**
     * Máxima dimensión (ancho o alto) permitida para Bitmaps de fondo de RemoteViews.
     * Capping la dimensión a 450 px preserva el aspecto exacto y la nitidez visual
     * mientras reduce el payload IPC Binder de ~2.5 MB a < 400 KB (previene TransactionTooLargeException).
     */
    private const val MAX_GLASS_BITMAP_DIMENSION = 450

    fun toBitmap(drawable: Drawable, width: Int = 128, height: Int = 128, scale: Float = 1.0f): Bitmap {
        val w = width.coerceIn(32, 256)
        val h = height.coerceIn(32, 256)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val padding = (w * (1f - scale.coerceIn(0.2f, 1.0f)) / 2f).toInt()
        drawable.setBounds(padding, padding, w - padding, h - padding)
        drawable.draw(canvas)
        
        return bitmap
    }

    fun createGlassBackgroundBitmap(
        widthPx: Int,
        heightPx: Int,
        baseColor: Int,
        opacity: Float,
        cornerRadiusDp: Float
    ): Bitmap {
        val rawW = widthPx.coerceAtLeast(80)
        val rawH = heightPx.coerceAtLeast(80)

        // Escalar preservando exactamente la proporción de aspecto (W : H)
        val maxDim = maxOf(rawW, rawH)
        val scaleFactor = if (maxDim > MAX_GLASS_BITMAP_DIMENSION) {
            MAX_GLASS_BITMAP_DIMENSION.toFloat() / maxDim.toFloat()
        } else {
            1.0f
        }

        val w = (rawW * scaleFactor).toInt().coerceAtLeast(40)
        val h = (rawH * scaleFactor).toInt().coerceAtLeast(40)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(2f, 2f, w - 2f, h - 2f)
        val cornerPx = cornerRadiusDp * 2.5f * scaleFactor

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val alpha = (opacity * 255).toInt().coerceIn(0, 255)
            color = (baseColor and 0xFFFFFF) or (alpha shl 24)
        }
        canvas.drawRoundRect(rect, cornerPx, cornerPx, paint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (3f * scaleFactor).coerceAtLeast(1.5f)
            val topAlpha = ((opacity + 0.4f).coerceAtMost(1f) * 255).toInt()
            val botAlpha = ((opacity + 0.1f).coerceAtMost(1f) * 255).toInt()
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.WHITE and 0xFFFFFF or (topAlpha shl 24), Color.WHITE and 0xFFFFFF or (botAlpha shl 24)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, cornerPx, cornerPx, strokePaint)

        return bitmap
    }

    fun createBatteryBarBitmap(
        widthPx: Int,
        heightPx: Int,
        level: Float,
        color: Int,
        cornerRadiusDp: Float
    ): Bitmap {
        val rawW = widthPx.coerceAtLeast(20)
        val rawH = heightPx.coerceAtLeast(6)

        val scaleFactor = if (rawW > 400) 400f / rawW.toFloat() else 1.0f
        val w = (rawW * scaleFactor).toInt().coerceAtLeast(20)
        val h = (rawH * scaleFactor).toInt().coerceAtLeast(6)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val cornerPx = cornerRadiusDp * 2.5f * scaleFactor

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = 0x40FFFFFF
        }
        val trackRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        canvas.drawRoundRect(trackRect, cornerPx, cornerPx, trackPaint)

        if (level > 0f) {
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color or -0x1000000
            }
            val progressRight = w * level.coerceIn(0f, 1f)
            val progressRect = RectF(0f, 0f, progressRight, h.toFloat())
            canvas.drawRoundRect(progressRect, cornerPx, cornerPx, progressPaint)
        }
        
        return bitmap
    }
}
