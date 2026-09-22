package com.privacyshield.app.privacy

import com.privacyshield.app.data.DetectionResult
import com.privacyshield.app.data.SensitiveRegion
import com.privacyshield.app.data.SensitiveType

object PrivacyPolicyEngine {

    data class FactualPrivacyReport(
        val totalDetected: Int,
        val totalProtected: Int,
        val isFullyProtected: Boolean,
        val metadataCleaned: Boolean,
        val originalPreserved: Boolean,
        val is100PercentOnDevice: Boolean,
        val processingTimeMs: Long,
        val protectionSummary: String
    )

    fun generateReport(result: DetectionResult): FactualPrivacyReport {
        val total = result.totalCount
        val protectedCount = result.protectedCount
        val fullyProtected = total > 0 && protectedCount == total

        val summary = when {
            total == 0 -> "No sensitive personal data detected."
            fullyProtected -> "$protectedCount/$total sensitive items protected"
            else -> "$protectedCount/$total sensitive items protected (some items excluded)"
        }

        return FactualPrivacyReport(
            totalDetected = total,
            totalProtected = protectedCount,
            isFullyProtected = fullyProtected,
            metadataCleaned = true,
            originalPreserved = true,
            is100PercentOnDevice = true,
            processingTimeMs = result.processingTimeMs,
            protectionSummary = summary
        )
    }

    fun getExplanationTitle(region: SensitiveRegion): String {
        return "Why was this flagged as ${region.type.title}?"
    }

    fun getContextBadgeText(type: SensitiveType): String {
        return when (type) {
            SensitiveType.AADHAAR -> "National ID • Critical"
            SensitiveType.PAN -> "Tax ID • Critical"
            SensitiveType.PHONE -> "Contact • High"
            SensitiveType.EMAIL -> "Electronic Mail • Medium"
            SensitiveType.UPI -> "Financial Handle • High"
            SensitiveType.BANK_ACCOUNT -> "Banking • Critical"
            SensitiveType.FACE -> "Biometric • Medium"
            SensitiveType.CUSTOM -> "Manual Selection"
        }
    }
}
