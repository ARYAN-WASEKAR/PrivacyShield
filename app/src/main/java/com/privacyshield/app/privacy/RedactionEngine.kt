package com.privacyshield.app.privacy

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.privacyshield.app.data.RedactionMode
import com.privacyshield.app.data.SensitiveRegion
import kotlin.math.max
import kotlin.math.min

object RedactionEngine {

    /**
     * Creates a new redacted Bitmap copy. Original bitmap is NEVER modified.
     */
    fun applyRedactions(
        sourceBitmap: Bitmap,
        regions: List<SensitiveRegion>,
        overrideMode: RedactionMode? = null
    ): Bitmap {
        val resultBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        for (region in regions) {
            if (!region.isEnabled) continue

            val mode = overrideMode ?: region.redactionType
            val clampedRect = clampRect(region.rect, resultBitmap.width, resultBitmap.height)
            if (clampedRect.width() <= 0 || clampedRect.height() <= 0) continue

            when (mode) {
                RedactionMode.BLACKOUT -> applyBlackout(canvas, clampedRect)
                RedactionMode.PIXELATE -> applyPixelation(resultBitmap, clampedRect)
                RedactionMode.BLUR -> applyBlur(resultBitmap, clampedRect)
            }
        }

        return resultBitmap
    }

    private fun clampRect(rect: Rect, maxWidth: Int, maxHeight: Int): Rect {
        val paddingX = (rect.width() * 0.04f).toInt().coerceAtLeast(2)
        val paddingY = (rect.height() * 0.08f).toInt().coerceAtLeast(2)

        val left = max(0, rect.left - paddingX)
        val top = max(0, rect.top - paddingY)
        val right = min(maxWidth, rect.right + paddingX)
        val bottom = min(maxHeight, rect.bottom + paddingY)

        return Rect(left, top, right, bottom)
    }

    private fun applyBlackout(canvas: Canvas, rect: Rect) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A") // Deep slate black
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val rectF = RectF(rect)
        val cornerRadius = min(rect.height() * 0.2f, 12f)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)
    }

    private fun applyPixelation(bitmap: Bitmap, rect: Rect) {
        val width = rect.width()
        val height = rect.height()
        if (width <= 0 || height <= 0) return

        val blockSize = max(8, min(width, height) / 6)

        for (y in rect.top until rect.bottom step blockSize) {
            for (x in rect.left until rect.right step blockSize) {
                val blockRight = min(x + blockSize, rect.right)
                val blockBottom = min(y + blockSize, rect.bottom)

                var rSum = 0L
                var gSum = 0L
                var bSum = 0L
                var count = 0

                for (by in y until blockBottom) {
                    for (bx in x until blockRight) {
                        val pixel = bitmap.getPixel(bx, by)
                        rSum += Color.red(pixel)
                        gSum += Color.green(pixel)
                        bSum += Color.blue(pixel)
                        count++
                    }
                }

                if (count > 0) {
                    val avgColor = Color.rgb(
                        (rSum / count).toInt(),
                        (gSum / count).toInt(),
                        (bSum / count).toInt()
                    )

                    for (by in y until blockBottom) {
                        for (bx in x until blockRight) {
                            bitmap.setPixel(bx, by, avgColor)
                        }
                    }
                }
            }
        }
    }

    private fun applyBlur(bitmap: Bitmap, rect: Rect) {
        val width = rect.width()
        val height = rect.height()
        if (width <= 2 || height <= 2) return

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, rect.left, rect.top, width, height)

        val radius = max(6, min(width, height) / 5)
        val blurred = fastBoxBlur(pixels, width, height, radius)

        bitmap.setPixels(blurred, 0, width, rect.left, rect.top, width, height)
    }

    /**
     * Multi-pass box blur for smooth, high-quality blurring on bitmap pixel arrays.
     */
    private fun fastBoxBlur(src: IntArray, w: Int, h: Int, radius: Int): IntArray {
        var current = src.clone()
        val temp = IntArray(w * h)

        // 3 passes of horizontal + vertical box blur approximates Gaussian blur
        repeat(3) {
            blurHorizontal(current, temp, w, h, radius)
            blurVertical(temp, current, w, h, radius)
        }
        return current
    }

    private fun blurHorizontal(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int) {
        for (y in 0 until h) {
            var rAcc = 0
            var gAcc = 0
            var bAcc = 0

            val rowStart = y * w

            for (i in -r..r) {
                val px = min(max(i, 0), w - 1)
                val c = src[rowStart + px]
                rAcc += (c shr 16) and 0xFF
                gAcc += (c shr 8) and 0xFF
                bAcc += c and 0xFF
            }

            val windowSize = 2 * r + 1

            for (x in 0 until w) {
                dst[rowStart + x] = (0xFF shl 24) or
                        ((rAcc / windowSize) shl 16) or
                        ((gAcc / windowSize) shl 8) or
                        (bAcc / windowSize)

                val leftIdx = min(max(x - r, 0), w - 1)
                val rightIdx = min(max(x + r + 1, 0), w - 1)

                val leftColor = src[rowStart + leftIdx]
                val rightColor = src[rowStart + rightIdx]

                rAcc += ((rightColor shr 16) and 0xFF) - ((leftColor shr 16) and 0xFF)
                gAcc += ((rightColor shr 8) and 0xFF) - ((leftColor shr 8) and 0xFF)
                bAcc += (rightColor and 0xFF) - (leftColor and 0xFF)
            }
        }
    }

    private fun blurVertical(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int) {
        for (x in 0 until w) {
            var rAcc = 0
            var gAcc = 0
            var bAcc = 0

            for (i in -r..r) {
                val py = min(max(i, 0), h - 1)
                val c = src[py * w + x]
                rAcc += (c shr 16) and 0xFF
                gAcc += (c shr 8) and 0xFF
                bAcc += c and 0xFF
            }

            val windowSize = 2 * r + 1

            for (y in 0 until h) {
                dst[y * w + x] = (0xFF shl 24) or
                        ((rAcc / windowSize) shl 16) or
                        ((gAcc / windowSize) shl 8) or
                        (bAcc / windowSize)

                val topIdx = min(max(y - r, 0), h - 1)
                val bottomIdx = min(max(y + r + 1, 0), h - 1)

                val topColor = src[topIdx * w + x]
                val bottomColor = src[bottomIdx * w + x]

                rAcc += ((bottomColor shr 16) and 0xFF) - ((topColor shr 16) and 0xFF)
                gAcc += ((bottomColor shr 8) and 0xFF) - ((topColor shr 8) and 0xFF)
                bAcc += (bottomColor and 0xFF) - (topColor and 0xFF)
            }
        }
    }
}
