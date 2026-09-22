package com.privacyshield.app.ai

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.privacyshield.app.data.RedactionMode
import com.privacyshield.app.data.SensitiveRegion
import com.privacyshield.app.data.SensitiveType
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FaceDetector {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
        FaceDetection.getClient(options)
    }

    suspend fun detectFaces(bitmap: Bitmap): List<SensitiveRegion> {
        val detected = mutableListOf<SensitiveRegion>()
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = detector.process(inputImage).await()

            for ((index, face) in faces.withIndex()) {
                val box = face.boundingBox
                val clampedRect = Rect(
                    box.left.coerceAtLeast(0),
                    box.top.coerceAtLeast(0),
                    box.right.coerceAtMost(bitmap.width),
                    box.bottom.coerceAtMost(bitmap.height)
                )

                if (clampedRect.width() > 10 && clampedRect.height() > 10) {
                    detected.add(
                        SensitiveRegion(
                            id = UUID.randomUUID().toString(),
                            type = SensitiveType.FACE,
                            originalText = "Face #${index + 1}",
                            maskedText = "[Facial Biometric Masked]",
                            rect = clampedRect,
                            confidence = 0.95f,
                            explanation = listOf(
                                "Human facial geometry detected",
                                "Protects biometric identity against unauthorized facial recognition"
                            ),
                            redactionType = RedactionMode.BLUR,
                            isEnabled = true
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if face detector encounters issues
        }
        return detected
    }
}
