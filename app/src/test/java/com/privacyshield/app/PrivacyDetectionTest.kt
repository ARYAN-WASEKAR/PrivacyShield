package com.privacyshield.app

import android.graphics.Rect
import com.privacyshield.app.ai.OcrEngine
import com.privacyshield.app.ai.PiiDetector
import com.privacyshield.app.data.DetectionResult
import com.privacyshield.app.data.SensitiveType
import com.privacyshield.app.privacy.PrivacyPolicyEngine
import com.privacyshield.app.privacy.Verhoeff
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyDetectionTest {

    @Test
    fun testVerhoeffAadhaarValidation() {
        // Valid Aadhaar numbers generated via Verhoeff checksum algorithm
        assertTrue(Verhoeff.validate("5834 2910 4722"))
        assertTrue(Verhoeff.validate("583429104722"))

        // Invalid numbers
        assertFalse(Verhoeff.validate("123456789012"))
        assertFalse(Verhoeff.validate("000000000000"))
        assertFalse(Verhoeff.validate("1234"))
    }

    @Test
    fun testPiiDetectorIdentifiesCoreRules() {
        val sampleText = """
            GOVERNMENT OF INDIA
            Income Tax Department
            Permanent Account Number: ABCDE1234F
            Aadhaar No: 5834 2910 4722
            Contact: +91 9876543210
            Email: user.test@example.com
            Pay via UPI: user@okhdfcbank
            Bank A/C No: 1234567890123
        """.trimIndent()

        val lines = sampleText.lines().mapIndexed { index, line ->
            OcrEngine.OcrLine(
                text = line,
                rect = Rect(0, index * 20, 300, (index + 1) * 20),
                elements = listOf(
                    OcrEngine.OcrElement(line, Rect(0, index * 20, 300, (index + 1) * 20))
                )
            )
        }

        val ocrResult = OcrEngine.OcrResult(
            fullText = sampleText,
            blocks = emptyList(),
            lines = lines,
            elements = emptyList()
        )

        val detections = PiiDetector.detect(ocrResult)

        // Verify PAN detection
        val pan = detections.firstOrNull { it.type == SensitiveType.PAN }
        assertTrue("PAN must be detected", pan != null)
        assertEquals("ABCDE1234F", pan?.originalText)

        // Verify Aadhaar detection
        val aadhaar = detections.firstOrNull { it.type == SensitiveType.AADHAAR }
        assertTrue("Aadhaar must be detected", aadhaar != null)

        // Verify Phone detection
        val phone = detections.firstOrNull { it.type == SensitiveType.PHONE }
        assertTrue("Phone must be detected", phone != null)

        // Verify Email detection
        val email = detections.firstOrNull { it.type == SensitiveType.EMAIL }
        assertTrue("Email must be detected", email != null)
        assertEquals("user.test@example.com", email?.originalText)

        // Verify UPI detection
        val upi = detections.firstOrNull { it.type == SensitiveType.UPI }
        assertTrue("UPI must be detected", upi != null)
        assertEquals("user@okhdfcbank", upi?.originalText)
    }

    @Test
    fun testPrivacyPolicyReport() {
        val result = DetectionResult(
            originalUri = null,
            originalBitmap = null,
            regions = emptyList(),
            processingTimeMs = 45,
            is100PercentOnDevice = true
        )

        val report = PrivacyPolicyEngine.generateReport(result)
        assertTrue(report.is100PercentOnDevice)
        assertTrue(report.metadataCleaned)
        assertTrue(report.originalPreserved)
    }
}
