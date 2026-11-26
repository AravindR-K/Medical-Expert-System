
package com.example.medexpertsystem.ui.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medexpertsystem.R
import com.example.medexpertsystem.data.ocr.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.example.medexpertsystem.data.ocr.RowType
import android.util.Log

@Composable
fun ImageProcessingTestScreen() {

    val context = LocalContext.current

    var originalBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var processedImage by remember {
        mutableStateOf<ProcessedImage?>(null)
    }

    var gridResult by remember {
        mutableStateOf<GridDetectionResult?>(null)
    }

    var gridCoordinates by remember {
        mutableStateOf<GridCoordinates?>(null)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var rowBoundaryResults by remember {
        mutableStateOf<List<RowBoundaryInfo>?>(null)
    }

    var tableExtractionResult by remember {
        mutableStateOf<TableExtractionResult?>(null)
    }
    var ncsReport by remember {
        mutableStateOf<NcsReport?>(null)
    }

    var ncsJson by remember {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.Default) {

                // 1. Load original NCS image without Android density scaling
                val options = BitmapFactory.Options().apply {
                    inScaled = false
                }

                val original = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ncs_sample,
                    options
                ) ?: throw Exception("Unable to load NCS image")

                // 2. Preprocess image
                val processed = ImagePreprocessor().preprocess(original)

                val grid = TableDetector().detectGrid(
                    processed.thresholded
                )

                // Extract row and column coordinates
                val coordinates = GridCoordinateExtractor().extract(
                    horizontalLines = grid.horizontalLines,
                    verticalLines = grid.verticalLines
                )

                val rowBoundaries = RowCellBoundaryDetector().detectRows(
                    verticalLinesBitmap = grid.verticalLines,
                    horizontalPositions = coordinates.horizontalPositions
                )

                Triple(
                    original,
                    processed,
                    Triple(grid, coordinates, rowBoundaries)
                )
            }

            originalBitmap = result.first
            processedImage = result.second

            gridResult = result.third.first
            gridCoordinates = result.third.second
            rowBoundaryResults = result.third.third

            // 9. Run ML Kit OCR on the original image
            val ocrProcessor = OcrProcessor()

            try {
                val ocrResult = ocrProcessor.processBitmap(result.first)

                // 10. Assign OCR text to detected table cells
                val extraction = TableCellExtractor().extract(
                    ocrResult = ocrResult,
                    rowBoundaries = result.third.third,
                    imageWidth = result.first.width,
                    imageHeight = result.first.height
                )

                tableExtractionResult = extraction
                // 11. Reconstruct the NCS report hierarchy
                val parsedReport = NcsReportParser().parse(extraction)

                ncsReport = parsedReport

                // 12. Convert reconstructed report into JSON
                val jsonOutput = NcsJsonSerializer().serialize(parsedReport)

                ncsJson = jsonOutput
                // Print complete JSON in Logcat
                val chunkSize = 3000

                jsonOutput.chunked(chunkSize).forEachIndexed { index, chunk ->
                    Log.d("NCS_JSON", "Part ${index + 1}:\n$chunk")
                }
            } finally {
                ocrProcessor.close()
            }

        } catch (exception: Exception) {
            error = exception.message ?: "Processing failed"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "OpenCV Table Detection Test",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
                Text("Detecting table grid...")
            }

            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {

                originalBitmap?.let { bitmap ->

                    gridCoordinates?.let { coordinates ->

                        CoordinateOverlay(
                            bitmap = bitmap,
                            coordinates = coordinates
                        )

                    } ?: ImageSection(
                        title = "1. Original NCS Image",
                        bitmap = bitmap
                    )
                }

                processedImage?.let {

                    ImageSection(
                        title = "2. Grayscale Image",
                        bitmap = it.grayscale
                    )

                    ImageSection(
                        title = "3. Adaptive Threshold Image",
                        bitmap = it.thresholded
                    )
                }

                gridResult?.let {

                    gridCoordinates?.let { coordinates ->

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Text(
                            text = "7. Extracted Grid Coordinates",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Image dimensions: " +
                                    "${coordinates.imageWidth} × ${coordinates.imageHeight}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Vertical Boundaries (${coordinates.verticalPositions.size})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = coordinates.verticalPositions.joinToString(", ")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Horizontal Boundaries (${coordinates.horizontalPositions.size})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = coordinates.horizontalPositions.joinToString(", ")
                        )


                        Text(
                            text = "Horizontal Boundaries (${coordinates.horizontalPositions.size})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = coordinates.horizontalPositions.joinToString(", ")
                        )

// =====================================
// 8. ROW-WISE CELL BOUNDARY DETECTION
// =====================================

                        rowBoundaryResults?.let { rows ->

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider()

                            Text(
                                text = "8. Row-wise Cell Boundary Detection (${rows.size})",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            rows.forEach { row ->

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {

                                        Text(
                                            text = "Row ${row.rowIndex + 1}",
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            text = "Y range: ${row.top} - ${row.bottom}"
                                        )

                                        Text(
                                            text = "Height: ${row.bottom - row.top} px"
                                        )

                                        Text(
                                            text = "Vertical boundaries (${row.verticalBoundaries.size}):"
                                        )

                                        Text(
                                            text = row.verticalBoundaries.joinToString(", ")
                                        )

                                        Text(
                                            text = when (row.rowType) {
                                                RowType.NORMAL_ROW ->
                                                    "Potential normal row"

                                                RowType.MERGED_ROW ->
                                                    "Potential merged row"

                                                RowType.UNSEGMENTED_ROW ->
                                                    "Unsegmented row"
                                            },
                                            color = when (row.rowType) {
                                                RowType.NORMAL_ROW ->
                                                    MaterialTheme.colorScheme.primary

                                                RowType.MERGED_ROW ->
                                                    MaterialTheme.colorScheme.tertiary

                                                RowType.UNSEGMENTED_ROW ->
                                                    MaterialTheme.colorScheme.error
                                            }
                                        )
                                    }
                                }
                            }
                            // =====================================
// 9. OCR TABLE CELL EXTRACTION
// =====================================

                            tableExtractionResult?.let { extraction ->

                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider()

                                Text(
                                    text = "9. Extracted Table Cell Values",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Extracted rows: ${extraction.rows.size}"
                                )

                                Text(
                                    text = "Unassigned OCR lines: ${extraction.unassignedLines.size}"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                extraction.rows.forEach { row ->

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    ) {

                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {

                                            Text(
                                                text = "Row ${row.rowIndex + 1}",
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            Text(
                                                text = "Y: ${row.top} - ${row.bottom}"
                                            )

                                            if (row.cells.isEmpty()) {

                                                Text("No cell boundaries detected")

                                            } else {

                                                row.cells.forEach { cell ->

                                                    Text(
                                                        text = "Column ${cell.columnIndex + 1}: " +
                                                                cell.text.ifBlank { "—" }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (extraction.unassignedLines.isNotEmpty()) {

                                    Text(
                                        text = "Unassigned OCR Text",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    extraction.unassignedLines.forEach { line ->

                                        Text(text = line.text)
                                    }

                                    // =====================================
// 10. RECONSTRUCTED NCS REPORT
// =====================================

                                    ncsReport?.let { report ->

                                        Spacer(modifier = Modifier.height(20.dp))

                                        HorizontalDivider()

                                        Text(
                                            text = "10. Reconstructed NCS Report",
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text("Total sections: ${report.sections.size}")

                                        report.sections.forEach { section ->

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp)
                                                ) {

                                                    Text(
                                                        text = section.sectionName,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )

                                                    Text(
                                                        text = "Measurements: ${section.measurements.size}"
                                                    )

                                                    section.measurements.forEach { measurement ->

                                                        HorizontalDivider(
                                                            modifier = Modifier.padding(vertical = 8.dp)
                                                        )

                                                        Text(
                                                            text = measurement.segment,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )

                                                        Text("Latency: ${measurement.latencyMs.rawText.ifBlank { "—" }}")
                                                        Text("Amplitude: ${measurement.amplitudeMv.rawText.ifBlank { "—" }}")
                                                        Text("Duration: ${measurement.durationMs.rawText.ifBlank { "—" }}")
                                                        Text("Latency Difference: ${measurement.latencyDifference.rawText.ifBlank { "—" }}")
                                                        Text("Distance: ${measurement.distanceMm.rawText.ifBlank { "—" }}")
                                                        Text("Conduction Velocity: ${measurement.conductionVelocity.rawText.ifBlank { "—" }}")
                                                    }
                                                }
                                            }
                                        }
                                        // =====================================
// 11. GENERATED JSON OUTPUT
// =====================================

                                        ncsJson?.let { json ->

                                            Spacer(modifier = Modifier.height(20.dp))

                                            HorizontalDivider()

                                            Text(
                                                text = "11. Generated NCS JSON",
                                                style = MaterialTheme.typography.titleLarge
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Card(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = json,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    ImageSection(
                        title = "4. Horizontal Lines",
                        bitmap = it.horizontalLines
                    )

                    ImageSection(
                        title = "5. Vertical Lines",
                        bitmap = it.verticalLines
                    )

                    ImageSection(
                        title = "6. Combined Grid Lines",
                        bitmap = it.combinedLines
                    )




                }
            }
        }
    }
}


@Composable
private fun CoordinateOverlay(
    bitmap: Bitmap,
    coordinates: GridCoordinates
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {

        Text(
            text = "1. Original Image with Grid Coordinates",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    bitmap.width.toFloat() / bitmap.height.toFloat()
                )
        ) {

            val image = bitmap.asImageBitmap()

            drawImage(
                image = image,
                dstSize = IntSize(
                    width = size.width.toInt(),
                    height = size.height.toInt()
                )
            )

            val scaleX = size.width / coordinates.imageWidth
            val scaleY = size.height / coordinates.imageHeight

            // Vertical boundaries - RED
            coordinates.verticalPositions.forEach { x ->

                val displayX = x * scaleX

                drawLine(
                    color = Color.Red,
                    start = Offset(displayX, 0f),
                    end = Offset(displayX, size.height),
                    strokeWidth = 2f
                )
            }

            // Horizontal boundaries - CYAN
            coordinates.horizontalPositions.forEach { y ->

                val displayY = y * scaleY

                drawLine(
                    color = Color.Cyan,
                    start = Offset(0f, displayY),
                    end = Offset(size.width, displayY),
                    strokeWidth = 2f
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "RED = Vertical boundaries | CYAN = Horizontal boundaries",
            style = MaterialTheme.typography.bodySmall
        )

        HorizontalDivider()
    }
}


@Composable
private fun ImageSection(
    title: String,
    bitmap: Bitmap
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = title,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()
    }
}
