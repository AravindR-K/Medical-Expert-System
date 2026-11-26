
package com.example.medexpertsystem.data.ocr

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

data class GridCoordinates(
    val verticalPositions: List<Int>,
    val horizontalPositions: List<Int>,
    val imageWidth: Int,
    val imageHeight: Int
)

class GridCoordinateExtractor {

    fun extract(
        horizontalLines: Bitmap,
        verticalLines: Bitmap
    ): GridCoordinates {

        val horizontalMat = Mat()
        val verticalMat = Mat()

        val horizontalGray = Mat()
        val verticalGray = Mat()

        try {
            // 1. Convert both bitmaps into OpenCV Mats
            Utils.bitmapToMat(horizontalLines, horizontalMat)
            Utils.bitmapToMat(verticalLines, verticalMat)

            // 2. Convert both images to grayscale
            Imgproc.cvtColor(
                horizontalMat,
                horizontalGray,
                Imgproc.COLOR_RGBA2GRAY
            )

            Imgproc.cvtColor(
                verticalMat,
                verticalGray,
                Imgproc.COLOR_RGBA2GRAY
            )

            val width = verticalGray.cols()
            val height = verticalGray.rows()

            // Ensure both masks have identical dimensions
            require(
                horizontalGray.cols() == width &&
                        horizontalGray.rows() == height
            ) {
                "Horizontal and vertical masks have different dimensions"
            }

            val verticalCounts = IntArray(width)
            val horizontalCounts = IntArray(height)

            val horizontalPixels = ByteArray(width)
            val verticalPixels = ByteArray(width)

            // 3. Count white pixels in horizontal mask
            for (y in 0 until height) {

                horizontalGray.get(y, 0, horizontalPixels)

                var count = 0

                for (x in 0 until width) {

                    val intensity =
                        horizontalPixels[x].toInt() and 0xFF

                    if (intensity > 127) {
                        count++
                    }
                }

                horizontalCounts[y] = count
            }

            // 4. Count white pixels in vertical mask
            for (y in 0 until height) {

                verticalGray.get(y, 0, verticalPixels)

                for (x in 0 until width) {

                    val intensity =
                        verticalPixels[x].toInt() and 0xFF

                    if (intensity > 127) {
                        verticalCounts[x]++
                    }
                }
            }

            // 5. Detect horizontal boundary candidates
            val horizontalCandidates = mutableListOf<Int>()

            for (y in 0 until height) {

                if (horizontalCounts[y] >= width * 0.50) {
                    horizontalCandidates.add(y)
                }
            }

            // 6. Detect vertical boundary candidates
            val verticalCandidates = mutableListOf<Int>()

            val verticalThreshold =
                (height * 0.01).toInt().coerceAtLeast(5)

            for (x in 0 until width) {

                if (verticalCounts[x] >= verticalThreshold) {
                    verticalCandidates.add(x)
                }
            }

            // 7. Group neighbouring coordinates
            return GridCoordinates(
                verticalPositions = groupCoordinates(
                    verticalCandidates
                ),
                horizontalPositions = groupCoordinates(
                    horizontalCandidates
                ),
                imageWidth = width,
                imageHeight = height
            )

        } finally {
            horizontalMat.release()
            verticalMat.release()
            horizontalGray.release()
            verticalGray.release()
        }
    }

    // Merge neighbouring pixels into one coordinate
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
