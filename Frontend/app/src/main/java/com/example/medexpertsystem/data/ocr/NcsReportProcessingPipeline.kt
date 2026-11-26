
package com.example.medexpertsystem.data.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri

data class NcsProcessingResult(
    val ocrResult: OcrResult,
    val tableExtractionResult: TableExtractionResult,
    val ncsReport: NcsReport,
    val json: String
)

class NcsReportProcessingPipeline {

    suspend fun process(
        context: Context,
        imageUri: Uri
    ): NcsProcessingResult {

        // 1. Load image from URI
        val bitmap = context.contentResolver
            .openInputStream(imageUri)
            ?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: throw Exception("Unable to load image")

        // 2. Preprocess image
        val processedImage =
            ImagePreprocessor().preprocess(bitmap)

        // 3. Detect table grid
        val grid = TableDetector().detectGrid(
            processedImage.thresholded
        )

        // 4. Extract grid coordinates
        val coordinates = GridCoordinateExtractor().extract(
            horizontalLines = grid.horizontalLines,
            verticalLines = grid.verticalLines
        )

        // 5. Detect row boundaries
        val rowBoundaries = RowCellBoundaryDetector().detectRows(
            verticalLinesBitmap = grid.verticalLines,
            horizontalPositions = coordinates.horizontalPositions
        )

        // 6. Perform OCR on original image
        val ocrProcessor = OcrProcessor()

        val ocrResult = try {
            ocrProcessor.processBitmap(bitmap)
        } finally {
            ocrProcessor.close()
        }

        // 7. Extract table cells
        val extraction = TableCellExtractor().extract(
            ocrResult = ocrResult,
            rowBoundaries = rowBoundaries,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height
        )

        // 8. Parse NCS report
        val report = NcsReportParser().parse(extraction)

        // 9. Generate JSON
        val json = NcsJsonSerializer().serialize(report)

        return NcsProcessingResult(
            ocrResult = ocrResult,
            tableExtractionResult = extraction,
            ncsReport = report,
            json = json
        )
    }
}
