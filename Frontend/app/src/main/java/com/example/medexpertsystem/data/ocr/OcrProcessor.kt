
package com.example.medexpertsystem.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Represents one recognised line of text
data class OcrLine(
    val text: String,
    val boundingBox: Rect?
)

// Represents a recognised text block
data class OcrBlock(
    val text: String,
    val boundingBox: Rect?,
    val lines: List<OcrLine>
)

// Complete OCR output
data class OcrResult(
    val fullText: String,
    val blocks: List<OcrBlock>
)

class OcrProcessor {

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    // Process an image from camera or gallery
    suspend fun processImage(
        context: Context,
        imageUri: Uri
    ): OcrResult {

        val image = InputImage.fromFilePath(context, imageUri)

        return recognize(image)
    }

    // Process a rendered PDF page
    suspend fun processBitmap(
        bitmap: Bitmap
    ): OcrResult {

        val image = InputImage.fromBitmap(bitmap, 0)

        return recognize(image)
    }

    // Common recognition logic
    private suspend fun recognize(
        image: InputImage
    ): OcrResult = suspendCancellableCoroutine { continuation ->

        recognizer.process(image)
            .addOnSuccessListener { visionText ->

                val blocks = visionText.textBlocks.map { block ->

                    OcrBlock(
                        text = block.text,
                        boundingBox = block.boundingBox,
                        lines = block.lines.map { line ->
                            OcrLine(
                                text = line.text,
                                boundingBox = line.boundingBox
                            )
                        }
                    )
                }

                if (continuation.isActive) {
                    continuation.resume(
                        OcrResult(
                            fullText = visionText.text,
                            blocks = blocks
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->

                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
    }

    // Release the recognizer when no longer needed
    fun close() {
        recognizer.close()
    }
}
