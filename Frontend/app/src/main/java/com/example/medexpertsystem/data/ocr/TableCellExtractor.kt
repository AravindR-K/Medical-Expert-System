
package com.example.medexpertsystem.data.ocr

import android.graphics.Rect

data class ExtractedTableCell(
    val rowIndex: Int,
    val columnIndex: Int,
    val bounds: Rect,
    val text: String,
    val sourceLines: List<OcrLine>
)

data class ExtractedTableRow(
    val rowIndex: Int,
    val top: Int,
    val bottom: Int,
    val rowType: RowType,
    val cells: List<ExtractedTableCell>
)

data class TableExtractionResult(
    val rows: List<ExtractedTableRow>,
    val unassignedLines: List<OcrLine>
)

class TableCellExtractor {

    fun extract(
        ocrResult: OcrResult,
        rowBoundaries: List<RowBoundaryInfo>,
        imageWidth: Int,
        imageHeight: Int
    ): TableExtractionResult {

        // Collect all OCR lines from all blocks.
        val allLines = ocrResult.blocks.flatMap { it.lines }

        // Keep track of lines assigned to cells.
        val assignedLines = mutableSetOf<OcrLine>()

        val extractedRows = rowBoundaries.map { row ->

            // Rows without detected column boundaries
            // cannot yet be divided into cells.
            if (row.verticalBoundaries.size < 2) {
                return@map ExtractedTableRow(
                    rowIndex = row.rowIndex,
                    top = row.top,
                    bottom = row.bottom,
                    rowType = row.rowType,
                    cells = emptyList()
                )
            }

            val xBoundaries = row.verticalBoundaries
                .distinct()
                .sorted()
                .map { it.coerceIn(0, imageWidth) }

            // Find OCR lines belonging to this row.
            val rowLines = allLines.filter { line ->

                val box = line.boundingBox ?: return@filter false

                val centerY = box.centerY()

                centerY >= row.top && centerY < row.bottom
            }

            // Create one cell between each pair
            // of consecutive vertical boundaries.
            val cells = xBoundaries.zipWithNext()
                .mapIndexed { columnIndex, (left, right) ->

                    val cellBounds = Rect(
                        left,
                        row.top.coerceIn(0, imageHeight),
                        right,
                        row.bottom.coerceIn(0, imageHeight)
                    )

                    // Assign OCR lines using their
                    // horizontal centre coordinates.
                    val cellLines = rowLines
                        .filter { line ->

                            val box = line.boundingBox
                                ?: return@filter false

                            val centerX = box.centerX()

                            centerX >= left && centerX < right
                        }
                        .sortedWith(
                            compareBy<OcrLine> {
                                it.boundingBox?.top ?: 0
                            }.thenBy {
                                it.boundingBox?.left ?: 0
                            }
                        )

                    assignedLines.addAll(cellLines)

                    ExtractedTableCell(
                        rowIndex = row.rowIndex,
                        columnIndex = columnIndex,
                        bounds = cellBounds,
                        text = cellLines.joinToString("\n") {
                            it.text
                        },
                        sourceLines = cellLines
                    )
                }

            ExtractedTableRow(
                rowIndex = row.rowIndex,
                top = row.top,
                bottom = row.bottom,
                rowType = row.rowType,
                cells = cells
            )
        }

        // Preserve OCR lines that could not be assigned.
        val unassigned = allLines.filter {
            it !in assignedLines
        }

        return TableExtractionResult(
            rows = extractedRows,
            unassignedLines = unassigned
        )
    }
}
