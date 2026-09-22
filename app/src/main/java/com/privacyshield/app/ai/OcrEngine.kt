package com.privacyshield.app.ai

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object OcrEngine {

    data class OcrElement(
        val text: String,
        val rect: Rect
    )

    data class OcrLine(
        val text: String,
        val rect: Rect,
        val elements: List<OcrElement>
    )

    data class OcrBlock(
        val text: String,
        val rect: Rect,
        val lines: List<OcrLine>
    )

    data class OcrResult(
        val fullText: String,
        val blocks: List<OcrBlock>,
        val lines: List<OcrLine>,
        val elements: List<OcrElement>
    )

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun processImage(bitmap: Bitmap): OcrResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val visionText: Text = recognizer.process(inputImage).await()

        val fullText = visionText.text
        val allBlocks = mutableListOf<OcrBlock>()
        val allLines = mutableListOf<OcrLine>()
        val allElements = mutableListOf<OcrElement>()

        for (block in visionText.textBlocks) {
            val blockRect = block.boundingBox ?: Rect(0, 0, 0, 0)
            val blockLines = mutableListOf<OcrLine>()

            for (line in block.lines) {
                val lineRect = line.boundingBox ?: Rect(0, 0, 0, 0)
                val lineElements = mutableListOf<OcrElement>()

                for (element in line.elements) {
                    val elemRect = element.boundingBox ?: Rect(0, 0, 0, 0)
                    val ocrElem = OcrElement(text = element.text, rect = elemRect)
                    lineElements.add(ocrElem)
                    allElements.add(ocrElem)
                }

                val ocrLine = OcrLine(
                    text = line.text,
                    rect = lineRect,
                    elements = lineElements
                )
                blockLines.add(ocrLine)
                allLines.add(ocrLine)
            }

            allBlocks.add(
                OcrBlock(
                    text = block.text,
                    rect = blockRect,
                    lines = blockLines
                )
            )
        }

        return OcrResult(
            fullText = fullText,
            blocks = allBlocks,
            lines = allLines,
            elements = allElements
        )
    }
}
