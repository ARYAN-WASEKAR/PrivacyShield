package com.privacyshield.app.data

import android.graphics.Rect

data class SensitiveRegion(
    val id: String,
    val type: SensitiveType,
    val originalText: String,
    val maskedText: String,
    val rect: Rect,
    val confidence: Float,
    val explanation: List<String>,
    val redactionType: RedactionMode = defaultModeFor(type),
    val isEnabled: Boolean = true
) {
    companion object {
        fun defaultModeFor(type: SensitiveType): RedactionMode {
            return when (type) {
                SensitiveType.AADHAAR,
                SensitiveType.PAN,
                SensitiveType.BANK_ACCOUNT -> RedactionMode.BLACKOUT
                SensitiveType.PHONE,
                SensitiveType.EMAIL,
                SensitiveType.UPI -> RedactionMode.BLACKOUT
                SensitiveType.FACE -> RedactionMode.BLUR
                SensitiveType.CUSTOM -> RedactionMode.BLACKOUT
            }
        }
    }
}
