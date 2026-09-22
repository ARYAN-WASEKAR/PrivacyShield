package com.privacyshield.app.data

enum class SensitiveType(
    val title: String,
    val description: String,
    val defaultConfidence: Float,
    val severity: Severity = Severity.HIGH
) {
    AADHAAR(
        title = "Aadhaar Number",
        description = "12-digit Indian national identity number",
        defaultConfidence = 0.98f,
        severity = Severity.CRITICAL
    ),
    PAN(
        title = "PAN Card Number",
        description = "10-character Indian tax identification number",
        defaultConfidence = 0.97f,
        severity = Severity.CRITICAL
    ),
    PHONE(
        title = "Phone Number",
        description = "Contact mobile or telephone number",
        defaultConfidence = 0.96f,
        severity = Severity.HIGH
    ),
    EMAIL(
        title = "Email Address",
        description = "Personal or business electronic mail address",
        defaultConfidence = 0.99f,
        severity = Severity.MEDIUM
    ),
    UPI(
        title = "UPI / Payment ID",
        description = "Virtual Payment Address / Payment Handle",
        defaultConfidence = 0.95f,
        severity = Severity.HIGH
    ),
    BANK_ACCOUNT(
        title = "Bank Account",
        description = "Bank account number / financial identifier",
        defaultConfidence = 0.94f,
        severity = Severity.CRITICAL
    ),
    FACE(
        title = "Human Face",
        description = "Facial biometric identity",
        defaultConfidence = 0.95f,
        severity = Severity.MEDIUM
    ),
    CUSTOM(
        title = "Custom Selection",
        description = "User-selected sensitive region",
        defaultConfidence = 1.0f,
        severity = Severity.HIGH
    )
}

enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
