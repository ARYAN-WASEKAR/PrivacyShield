package com.privacyshield.app.ai

import android.graphics.Rect
import com.privacyshield.app.data.SensitiveRegion
import com.privacyshield.app.data.SensitiveType
import com.privacyshield.app.privacy.Verhoeff
import java.util.UUID

object PiiDetector {

    private val AADHAAR_SPACED_REGEX = Regex("""\b([2-9]\d{3})\s+(\d{4})\s+(\d{4})\b""")
    private val AADHAAR_CONTINUOUS_REGEX = Regex("""\b([2-9]\d{11})\b""")

    private val PAN_REGEX = Regex("""\b([A-Z]{5}[0-9]{4}[A-Z])\b""")

    private val INDIAN_PHONE_REGEX = Regex("""\b(?:(?:\+?91[\-\s]?)?[6-9]\d{9})\b""")
    private val GENERAL_PHONE_REGEX = Regex("""\b(?:\+?1[\-\s]?)?\(?\d{3}\)?[\-\s]?\d{3}[\-\s]?\d{4}\b""")

    private val EMAIL_REGEX = Regex("""\b([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})\b""")

    private val KNOWN_UPI_HANDLES = setOf(
        "okhdfcbank", "oksbi", "okaxis", "okicici", "paytm", "ybl", "ibl",
        "upi", "apl", "axl", "sbi", "hdfcbank", "icici", "kotak", "barodampay",
        "postbank", "idfcbank", "federal", "airtel", "freecharge", "jupiteraxis",
        "fbl", "indus", "pingpay", "waaxis", "wahdfc", "waicici", "wasbi"
    )
    private val UPI_GENERIC_REGEX = Regex("""\b([a-zA-Z0-9.\-_]{2,64}@([a-zA-Z]{2,32}))\b""")

    private val BANK_ACCOUNT_REGEX = Regex("""\b(\d{9,18})\b""")

    // Context dictionaries for intelligent verification
    private val AADHAAR_KEYWORDS = listOf("aadhaar", "uidai", "mera aadhaar", "unique identification", "government of india", "govt of india", "vid", "enrollment", "dob", "yob", "male", "female")
    private val PAN_KEYWORDS = listOf("income tax", "permanent account number", "govt of india", "father's name", "incometax", "signature", "nsdl", "utiitsl")
    private val PHONE_KEYWORDS = listOf("mob", "mobile", "phone", "tel", "call", "contact", "cell", "whatsapp", "+91", "ph:")
    private val UPI_KEYWORDS = listOf("upi", "pay", "gpay", "phonepe", "bhim", "vpa", "scan", "payment", "id:")
    private val BANK_KEYWORDS = listOf("account", "a/c", "ac no", "acct", "savings", "current", "ifsc", "branch", "bank", "sbi", "hdfc", "icici", "pnb", "axis", "cif")

    fun detect(ocrResult: OcrEngine.OcrResult): List<SensitiveRegion> {
        val detected = mutableListOf<SensitiveRegion>()
        val fullTextLower = ocrResult.fullText.lowercase()

        val hasAadhaarContext = AADHAAR_KEYWORDS.any { fullTextLower.contains(it) }
        val hasPanContext = PAN_KEYWORDS.any { fullTextLower.contains(it) }
        val hasPhoneContext = PHONE_KEYWORDS.any { fullTextLower.contains(it) }
        val hasUpiContext = UPI_KEYWORDS.any { fullTextLower.contains(it) }
        val hasBankContext = BANK_KEYWORDS.any { fullTextLower.contains(it) }

        // Track already covered bounding boxes to avoid redundant overlapping detections
        val matchedTokens = mutableSetOf<String>()

        for (line in ocrResult.lines) {
            val lineText = line.text
            val lineLower = lineText.lowercase()
            val lineHasPhoneContext = PHONE_KEYWORDS.any { lineLower.contains(it) } || hasPhoneContext
            val lineHasBankContext = BANK_KEYWORDS.any { lineLower.contains(it) } || hasBankContext
            val lineHasUpiContext = UPI_KEYWORDS.any { lineLower.contains(it) } || hasUpiContext

            // 1. AADHAAR DETECTION (4-4-4 or 12-digit)
            AADHAAR_SPACED_REGEX.findAll(lineText).forEach { match ->
                val raw = match.value
                val clean = raw.replace("\\s+".toRegex(), "")
                val verhoeffValid = Verhoeff.validate(clean)

                val explanations = mutableListOf(
                    "Matches standard 12-digit UIDAI 4-4-4 spacing pattern"
                )
                var conf = 0.95f

                if (verhoeffValid) {
                    explanations.add("Mathematically validated by Verhoeff checksum algorithm")
                    conf += 0.03f
                }
                if (hasAadhaarContext) {
                    explanations.add("Surrounding context matches national identity document keywords (Aadhaar/UIDAI/Govt of India)")
                    conf += 0.01f
                }

                matchedTokens.add(clean)
                val rect = computeMatchRect(line, match.range)
                detected.add(
                    SensitiveRegion(
                        id = UUID.randomUUID().toString(),
                        type = SensitiveType.AADHAAR,
                        originalText = raw,
                        maskedText = maskAadhaar(clean),
                        rect = rect,
                        confidence = conf.coerceAtMost(0.99f),
                        explanation = explanations
                    )
                )
            }

            AADHAAR_CONTINUOUS_REGEX.findAll(lineText).forEach { match ->
                val raw = match.value
                if (!matchedTokens.contains(raw)) {
                    val verhoeffValid = Verhoeff.validate(raw)
                    if (verhoeffValid || hasAadhaarContext) {
                        val explanations = mutableListOf("12-digit consecutive number sequence")
                        var conf = 0.92f

                        if (verhoeffValid) {
                            explanations.add("Valid Verhoeff identity checksum")
                            conf += 0.05f
                        }
                        if (hasAadhaarContext) {
                            explanations.add("Proximity to UIDAI / Government of India context")
                            conf += 0.02f
                        }

                        matchedTokens.add(raw)
                        val rect = computeMatchRect(line, match.range)
                        detected.add(
                            SensitiveRegion(
                                id = UUID.randomUUID().toString(),
                                type = SensitiveType.AADHAAR,
                                originalText = raw,
                                maskedText = maskAadhaar(raw),
                                rect = rect,
                                confidence = conf.coerceAtMost(0.99f),
                                explanation = explanations
                            )
                        )
                    }
                }
            }

            // 2. PAN CARD DETECTION
            PAN_REGEX.findAll(lineText).forEach { match ->
                val pan = match.value
                val entityChar = pan[3]
                val validEntities = "CPHFATBLJG"
                val isValidEntity = validEntities.contains(entityChar)

                val explanations = mutableListOf(
                    "Matches standard 10-character Indian PAN format (5 letters + 4 digits + 1 letter)"
                )
                var conf = 0.95f

                if (isValidEntity) {
                    explanations.add("4th character '$entityChar' matches registered Indian entity type")
                    conf += 0.02f
                }
                if (hasPanContext) {
                    explanations.add("Surrounding context matches Income Tax Department / Tax Identification keywords")
                    conf += 0.02f
                }

                matchedTokens.add(pan)
                val rect = computeMatchRect(line, match.range)
                detected.add(
                    SensitiveRegion(
                        id = UUID.randomUUID().toString(),
                        type = SensitiveType.PAN,
                        originalText = pan,
                        maskedText = maskPan(pan),
                        rect = rect,
                        confidence = conf.coerceAtMost(0.99f),
                        explanation = explanations
                    )
                )
            }

            // 3. EMAIL ADDRESS DETECTION
            EMAIL_REGEX.findAll(lineText).forEach { match ->
                val email = match.value
                matchedTokens.add(email)
                matchedTokens.add(email.substringBeforeLast("."))
                val rect = computeMatchRect(line, match.range)
                detected.add(
                    SensitiveRegion(
                        id = UUID.randomUUID().toString(),
                        type = SensitiveType.EMAIL,
                        originalText = email,
                        maskedText = maskEmail(email),
                        rect = rect,
                        confidence = 0.99f,
                        explanation = listOf(
                            "Standard RFC electronic mail format with verified top-level domain",
                            "High risk of personal identity exposure and phishing target"
                        )
                    )
                )
            }

            // 4. UPI / PAYMENT ID DETECTION
            UPI_GENERIC_REGEX.findAll(lineText).forEach { match ->
                val upi = match.value
                val handle = match.groupValues[2].lowercase()
                val isKnownHandle = KNOWN_UPI_HANDLES.contains(handle)

                if ((isKnownHandle || lineHasUpiContext) && !matchedTokens.contains(upi)) {
                    val explanations = mutableListOf("Virtual Payment Address (VPA) / UPI handle syntax")
                    var conf = 0.94f

                    if (isKnownHandle) {
                        explanations.add("Recognized payment service provider handle: @$handle")
                        conf += 0.04f
                    }
                    if (lineHasUpiContext) {
                        explanations.add("Surrounding text indicates payment transaction or QR pay context")
                        conf += 0.01f
                    }

                    matchedTokens.add(upi)
                    val rect = computeMatchRect(line, match.range)
                    detected.add(
                        SensitiveRegion(
                            id = UUID.randomUUID().toString(),
                            type = SensitiveType.UPI,
                            originalText = upi,
                            maskedText = maskUpi(upi),
                            rect = rect,
                            confidence = conf.coerceAtMost(0.99f),
                            explanation = explanations
                        )
                    )
                }
            }

            // 5. PHONE NUMBER DETECTION
            INDIAN_PHONE_REGEX.findAll(lineText).forEach { match ->
                val raw = match.value
                val digitsOnly = raw.replace("\\D".toRegex(), "")
                val coreNumber = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly

                if (!matchedTokens.contains(digitsOnly) && !matchedTokens.contains(coreNumber)) {
                    val isFirstDigitValid = coreNumber.isNotEmpty() && coreNumber[0] in '6'..'9'
                    if (isFirstDigitValid && (lineHasPhoneContext || raw.startsWith("+91") || raw.length == 10)) {
                        val explanations = mutableListOf(
                            "10-digit mobile number format starting with [6-9]"
                        )
                        var conf = 0.95f
                        if (lineHasPhoneContext) {
                            explanations.add("Phone/Contact keyword cue detected in vicinity")
                            conf += 0.02f
                        }
                        if (raw.startsWith("+91")) {
                            explanations.add("Contains India country code prefix (+91)")
                            conf += 0.02f
                        }

                        matchedTokens.add(coreNumber)
                        val rect = computeMatchRect(line, match.range)
                        detected.add(
                            SensitiveRegion(
                                id = UUID.randomUUID().toString(),
                                type = SensitiveType.PHONE,
                                originalText = raw,
                                maskedText = maskPhone(raw),
                                rect = rect,
                                confidence = conf.coerceAtMost(0.99f),
                                explanation = explanations
                            )
                        )
                    }
                }
            }

            // 6. BANK ACCOUNT DETECTION (Strict contextual requirement)
            if (lineHasBankContext) {
                BANK_ACCOUNT_REGEX.findAll(lineText).forEach { match ->
                    val acc = match.value
                    if (!matchedTokens.contains(acc) && acc.length !in 10..12 || !matchedTokens.contains(acc)) {
                        // Avoid Aadhaar or Phone re-tagging as Bank Acc
                        val isAadhaarCandidate = acc.length == 12 && Verhoeff.validate(acc)
                        val isPhoneCandidate = acc.length == 10 && acc[0] in '6'..'9'

                        if (!isAadhaarCandidate && !isPhoneCandidate) {
                            matchedTokens.add(acc)
                            val rect = computeMatchRect(line, match.range)
                            detected.add(
                                SensitiveRegion(
                                    id = UUID.randomUUID().toString(),
                                    type = SensitiveType.BANK_ACCOUNT,
                                    originalText = acc,
                                    maskedText = maskBankAccount(acc),
                                    rect = rect,
                                    confidence = 0.94f,
                                    explanation = listOf(
                                        "${acc.length}-digit financial account number sequence",
                                        "Detected near banking context keywords (A/C, Account, IFSC, Savings)"
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }

        return detected
    }

    private fun computeMatchRect(line: OcrEngine.OcrLine, range: IntRange): Rect {
        val lineLen = line.text.length.coerceAtLeast(1)
        val startFraction = range.first.toFloat() / lineLen
        val endFraction = (range.last + 1).toFloat() / lineLen

        val lineRect = line.rect
        val left = (lineRect.left + lineRect.width() * startFraction).toInt()
        val right = (lineRect.left + lineRect.width() * endFraction).toInt()

        return Rect(left, lineRect.top, right, lineRect.bottom)
    }

    private fun maskAadhaar(num: String): String {
        val clean = num.replace("\\s+".toRegex(), "")
        return if (clean.length >= 12) {
            "XXXX-XXXX-" + clean.takeLast(4)
        } else {
            "XXXX-XXXX-XXXX"
        }
    }

    private fun maskPan(pan: String): String {
        return if (pan.length == 10) {
            pan.take(2) + "XXXXX" + pan.takeLast(3)
        } else {
            "XXXXXXXXXX"
        }
    }

    private fun maskPhone(phone: String): String {
        val digits = phone.replace("\\D".toRegex(), "")
        return if (digits.length >= 10) {
            "+91 XXXXX " + digits.takeLast(4)
        } else {
            "XXXXX XXXXX"
        }
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size == 2) {
            val name = parts[0]
            val domain = parts[1]
            val maskedName = if (name.length > 2) name.take(2) + "***" else "***"
            return "$maskedName@$domain"
        }
        return "***@***.com"
    }

    private fun maskUpi(upi: String): String {
        val parts = upi.split("@")
        if (parts.size == 2) {
            val handle = parts[0]
            val psp = parts[1]
            val maskedHandle = if (handle.length > 2) handle.take(2) + "***" else "***"
            return "$maskedHandle@$psp"
        }
        return "***@upi"
    }

    private fun maskBankAccount(acc: String): String {
        return if (acc.length > 4) {
            "X".repeat(acc.length - 4) + acc.takeLast(4)
        } else {
            "XXXX"
        }
    }
}
