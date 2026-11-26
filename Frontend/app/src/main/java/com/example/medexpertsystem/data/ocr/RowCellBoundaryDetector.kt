
package com.example.medexpertsystem.data.ocr

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import kotlin.math.ceil

// Three possible geometric row classifications
enum class RowType {
    NORMAL_ROW,
    MERGED_ROW,
    UNSEGMENTED_ROW
}

data class RowBoundaryInfo(
    val rowIndex: Int,
    val top: Int,
    val bottom: Int,
    val verticalBoundaries: List<Int>,
    val rowType: RowType
)

class RowCellBoundaryDetector {

    fun detectRows(
        verticalLinesBitmap: Bitmap,
        horizontalPositions: List<Int>
    ): List<RowBoundaryInfo> {

        val source = Mat()
        val grayscale = Mat()

        try {
            Utils.bitmapToMat(verticalLinesBitmap, source)

            Imgproc.cvtColor(
                source,
                grayscale,
                Imgproc.COLOR_RGBA2GRAY
            )

            val width = grayscale.cols()
            val height = grayscale.rows()

            val rowPixels = ByteArray(width)

            val results = mutableListOf<RowBoundaryInfo>()

            for (i in 0 until horizontalPositions.size - 1) {

                val top = horizontalPositions[i]
                    .coerceIn(0, height - 1)

                val bottom = horizontalPositions[i + 1]
                    .coerceIn(0, height)

                val rowHeight = bottom - top

                if (rowHeight <= 4) continue

                val verticalCounts = IntArray(width)

                val startY = (top + 2).coerceAtMost(bottom)
                val endY = (bottom - 2).coerceAtLeast(startY)

                for (y in startY until endY) {

                    grayscale.get(y, 0, rowPixels)

                    for (x in 0 until width) {

                        val intensity =
                            rowPixels[x].toInt() and 0xFF

                        if (intensity > 127) {
                            verticalCounts[x]++
                        }
                    }
                }

                val minPixels = ceil(
                    (endY - startY) * 0.35
                ).toInt().coerceAtLeast(3)

                val candidates = mutableListOf<Int>()

                for (x in 0 until width) {

                    if (verticalCounts[x] >= minPixels) {
                        candidates.add(x)
                    }
                }

                val boundaries = groupCoordinates(candidates)

                // Classify based on detected vertical boundaries
// Separate outer table borders from internal column boundaries
                val internalBoundaries = if (boundaries.size >= 2) {
                    val leftBorder = boundaries.first()
                    val rightBorder = boundaries.last()

                    boundaries.filter { position ->
                        position > leftBorder + 5 &&
                                position < rightBorder - 5
                    }
                } else {
                    emptyList()
                }

// Classify using internal boundaries
                val rowType = when {
                    internalBoundaries.size >= 2 ->
                        RowType.NORMAL_ROW

                    internalBoundaries.isEmpty() && boundaries.size >= 2 ->
                        RowType.MERGED_ROW

                    else ->
                        RowType.UNSEGMENTED_ROW
                }

                results.add(
                    RowBoundaryInfo(
                        rowIndex = i,
                        top = top,
                        bottom = bottom,
                        verticalBoundaries = boundaries,
                        rowType = rowType
                    )
                )
            }

            return results

        } finally {
            source.release()
            grayscale.release()
        }
    }

    private fun groupCoordinates(
        positions: List<Int>
    ): List<Int> {

        if (positions.isEmpty()) return emptyList()

        val result = mutableListOf<Int>()

        var sum = positions.first()
        var count = 1
        var previous = positions.first()

        for (i in 1 until positions.size) {

            val current = positions[i]

            if (current <= previous + 2) {
                sum += current
                count++
            } else {
                result.add(sum / count)
                sum = current
                count = 1
            }

            previous = current
        }

        result.add(sum / count)

        return result
    }
}
