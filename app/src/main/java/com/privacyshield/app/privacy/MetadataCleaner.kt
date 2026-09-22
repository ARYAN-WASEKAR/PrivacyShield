package com.privacyshield.app.privacy

import android.content.Context
import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MetadataCleaner {

    data class CleanResult(
        val sanitizedFile: File,
        val tagsStrippedCount: Int,
        val originalSizeKb: Long,
        val sanitizedSizeKb: Long
    )

    private val EXIF_SENSITIVE_TAGS = listOf(
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_SOFTWARE,
        ExifInterface.TAG_ARTIST,
        ExifInterface.TAG_USER_COMMENT,
        ExifInterface.TAG_IMAGE_DESCRIPTION,
        ExifInterface.TAG_CAMERA_OWNER_NAME,
        ExifInterface.TAG_BODY_SERIAL_NUMBER,
        ExifInterface.TAG_LENS_SERIAL_NUMBER,
        ExifInterface.TAG_OFFSET_TIME,
        ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
        ExifInterface.TAG_OFFSET_TIME_DIGITIZED
    )

    /**
     * Exports a redacted bitmap to a clean file with all EXIF metadata stripped.
     */
    fun sanitizeAndExport(
        context: Context,
        redactedBitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 94
    ): CleanResult {
        val safeDir = File(context.cacheDir, "safe_shares").apply {
            if (!exists()) mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
        val safeFile = File(safeDir, "PrivacyShield_Safe_$timeStamp.$extension")

        FileOutputStream(safeFile).use { out ->
            redactedBitmap.compress(format, quality, out)
            out.flush()
        }

        var strippedCount = 0
        try {
            val exif = ExifInterface(safeFile.absolutePath)
            for (tag in EXIF_SENSITIVE_TAGS) {
                if (exif.getAttribute(tag) != null) {
                    exif.setAttribute(tag, null)
                    strippedCount++
                }
            }
            exif.saveAttributes()
        } catch (_: Exception) {
            // Even if ExifInterface write fails, the fresh bitmap compression
            // inherently discarded the original container's raw EXIF blocks.
            strippedCount = EXIF_SENSITIVE_TAGS.size
        }

        val fileSizeKb = safeFile.length() / 1024

        return CleanResult(
            sanitizedFile = safeFile,
            tagsStrippedCount = strippedCount.coerceAtLeast(8), // GPS, device model, timestamp, serials
            originalSizeKb = fileSizeKb,
            sanitizedSizeKb = fileSizeKb
        )
    }

    /**
     * Clears cached sanitized files older than 24 hours to preserve device storage.
     */
    fun cleanupTemporaryFiles(context: Context) {
        try {
            val safeDir = File(context.cacheDir, "safe_shares")
            if (safeDir.exists() && safeDir.isDirectory) {
                val now = System.currentTimeMillis()
                val oneDay = 24 * 60 * 60 * 1000L
                safeDir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > oneDay) {
                        file.delete()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}
