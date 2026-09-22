package com.privacyshield.app.data

import android.graphics.Bitmap
import android.net.Uri

data class DetectionResult(
    val originalUri: Uri?,
    val originalBitmap: Bitmap?,
    val regions: List<SensitiveRegion>,
    val metadataTagsStripped: Int = 0,
    val processingTimeMs: Long = 0,
    val is100PercentOnDevice: Boolean = true
) {
    val enabledRegions: List<SensitiveRegion>
        get() = regions.filter { it.isEnabled }

    val totalCount: Int
        get() = regions.size

    val protectedCount: Int
        get() = enabledRegions.size
}
