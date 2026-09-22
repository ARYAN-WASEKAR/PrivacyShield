package com.privacyshield.app.data

enum class RedactionMode(
    val title: String,
    val description: String
) {
    BLACKOUT(
        title = "Blackout",
        description = "Solid opaque dark mask with rounded borders. Recommended for high-risk PII."
    ),
    PIXELATE(
        title = "Pixelate",
        description = "Mosaic tile sampling to scramble details while preserving surrounding aesthetic."
    ),
    BLUR(
        title = "Blur",
        description = "Smooth Gaussian-like box blur to obscure text and facial biometrics."
    )
}
