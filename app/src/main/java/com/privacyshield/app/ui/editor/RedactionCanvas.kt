package com.privacyshield.app.ui.editor

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.privacyshield.app.data.SensitiveRegion
import com.privacyshield.app.data.SensitiveType
import com.privacyshield.app.ui.theme.CyanAccent
import com.privacyshield.app.ui.theme.EmeraldPrimary
import com.privacyshield.app.ui.theme.PurpleAadhaar
import com.privacyshield.app.ui.theme.RoseAlert
import kotlin.math.max
import kotlin.math.min

@Composable
fun RedactionCanvas(
    redactedBitmap: Bitmap,
    originalBitmap: Bitmap,
    regions: List<SensitiveRegion>,
    onRegionTapped: (SensitiveRegion) -> Unit,
    onCustomRegionAdded: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

    val imageBitmap = remember(redactedBitmap) { redactedBitmap.asImageBitmap() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(regions) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val bmpW = originalBitmap.width.toFloat()
                        val bmpH = originalBitmap.height.toFloat()

                        val scale = min(canvasW / bmpW, canvasH / bmpH)
                        val offsetX = (canvasW - bmpW * scale) / 2f
                        val offsetY = (canvasH - bmpH * scale) / 2f

                        // Convert tap to bitmap coordinate space
                        val bmpX = (tapOffset.x - offsetX) / scale
                        val bmpY = (tapOffset.y - offsetY) / scale

                        val hit = regions.firstOrNull { region ->
                            region.rect.contains(bmpX.toInt(), bmpY.toInt())
                        }

                        if (hit != null) {
                            onRegionTapped(hit)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStartOffset = offset
                            dragCurrentOffset = offset
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragCurrentOffset = change.position
                        },
                        onDragEnd = {
                            val start = dragStartOffset
                            val end = dragCurrentOffset
                            if (start != null && end != null) {
                                val canvasW = size.width.toFloat()
                                val canvasH = size.height.toFloat()
                                val bmpW = originalBitmap.width.toFloat()
                                val bmpH = originalBitmap.height.toFloat()

                                val scale = min(canvasW / bmpW, canvasH / bmpH)
                                val offsetX = (canvasW - bmpW * scale) / 2f
                                val offsetY = (canvasH - bmpH * scale) / 2f

                                val left = min(start.x, end.x)
                                val top = min(start.y, end.y)
                                val right = max(start.x, end.x)
                                val bottom = max(start.y, end.y)

                                if (right - left > 20 && bottom - top > 20) {
                                    val bmpLeft = ((left - offsetX) / scale).toInt().coerceAtLeast(0)
                                    val bmpTop = ((top - offsetY) / scale).toInt().coerceAtLeast(0)
                                    val bmpRight = ((right - offsetX) / scale).toInt().coerceAtMost(originalBitmap.width)
                                    val bmpBottom = ((bottom - offsetY) / scale).toInt().coerceAtMost(originalBitmap.height)

                                    if (bmpRight > bmpLeft && bmpBottom > bmpTop) {
                                        onCustomRegionAdded(Rect(bmpLeft, bmpTop, bmpRight, bmpBottom))
                                    }
                                }
                            }
                            dragStartOffset = null
                            dragCurrentOffset = null
                        },
                        onDragCancel = {
                            dragStartOffset = null
                            dragCurrentOffset = null
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val bmpW = originalBitmap.width.toFloat()
            val bmpH = originalBitmap.height.toFloat()

            val scale = min(canvasW / bmpW, canvasH / bmpH)
            val scaledW = bmpW * scale
            val scaledH = bmpH * scale
            val offsetX = (canvasW - scaledW) / 2f
            val offsetY = (canvasH - scaledH) / 2f

            // Draw current redacted bitmap
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                dstSize = IntSize(scaledW.toInt(), scaledH.toInt())
            )

            // Draw bounding box indicators with labels
            for (region in regions) {
                val rect = region.rect
                val boxLeft = offsetX + rect.left * scale
                val boxTop = offsetY + rect.top * scale
                val boxW = rect.width() * scale
                val boxH = rect.height() * scale

                val strokeColor = when (region.type) {
                    SensitiveType.AADHAAR -> PurpleAadhaar
                    SensitiveType.PAN -> CyanAccent
                    SensitiveType.PHONE -> EmeraldPrimary
                    SensitiveType.EMAIL -> Color(0xFF3B82F6)
                    SensitiveType.UPI -> Color(0xFF8B5CF6)
                    SensitiveType.BANK_ACCOUNT -> RoseAlert
                    SensitiveType.FACE -> Color(0xFFEC4899)
                    SensitiveType.CUSTOM -> Color(0xFFF59E0B)
                }

                val typeLabel = when (region.type) {
                    SensitiveType.AADHAAR -> "AADHAAR"
                    SensitiveType.PAN -> "PAN"
                    SensitiveType.PHONE -> "PHONE"
                    SensitiveType.EMAIL -> "EMAIL"
                    SensitiveType.UPI -> "UPI"
                    SensitiveType.BANK_ACCOUNT -> "BANK A/C"
                    SensitiveType.FACE -> "FACE"
                    SensitiveType.CUSTOM -> "CUSTOM"
                }

                if (region.isEnabled) {
                    drawRoundRect(
                        color = strokeColor.copy(alpha = 0.9f),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Draw text label badge
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 10.dp.toPx()
                        typeface = Typeface.create(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                        )
                        isAntiAlias = true
                    }
                    val textWidth = textPaint.measureText(typeLabel)
                    val textHeight = textPaint.textSize
                    val paddingX = 5.dp.toPx()
                    val paddingY = 2.dp.toPx()

                    val labelLeft = boxLeft
                    val labelTop = (boxTop - textHeight - paddingY * 2).coerceAtLeast(0f)
                    val labelRight = labelLeft + textWidth + paddingX * 2
                    val labelBottom = labelTop + textHeight + paddingY * 2

                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        labelLeft,
                        labelTop,
                        labelRight,
                        labelBottom,
                        4.dp.toPx(),
                        4.dp.toPx(),
                        Paint().apply {
                            color = strokeColor.toArgb()
                            isAntiAlias = true
                        }
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        typeLabel,
                        labelLeft + paddingX,
                        labelBottom - paddingY - 1.dp.toPx(),
                        textPaint
                    )
                } else {
                    // Dimmed border when disabled
                    drawRoundRect(
                        color = Color(0x66FFFFFF),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Draw interactive user drag preview
            val start = dragStartOffset
            val end = dragCurrentOffset
            if (start != null && end != null) {
                val dragLeft = min(start.x, end.x)
                val dragTop = min(start.y, end.y)
                val dragW = max(start.x, end.x) - dragLeft
                val dragH = max(start.y, end.y) - dragTop

                drawRoundRect(
                    color = EmeraldPrimary.copy(alpha = 0.3f),
                    topLeft = Offset(dragLeft, dragTop),
                    size = Size(dragW, dragH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = EmeraldPrimary,
                    topLeft = Offset(dragLeft, dragTop),
                    size = Size(dragW, dragH),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
