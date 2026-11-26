package com.example.medexpertsystem.data.ocr


import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

data class GridDetectionResult(
    val horizontalLines: Bitmap,
    val verticalLines: Bitmap,
    val combinedLines: Bitmap
)

class TableDetector {

    fun detectGrid(thresholdedBitmap: Bitmap): GridDetectionResult {

        val source = Mat()
        val grayscale = Mat()
        val inverted = Mat()

        val horizontal = Mat()
        val vertical = Mat()
        val combined = Mat()

        val horizontalKernel: Mat
        val verticalKernel: Mat

        try {
            // Step 1: Convert Bitmap to OpenCV Mat
            Utils.bitmapToMat(thresholdedBitmap, source)

            // Step 2: Convert RGBA to grayscale
            Imgproc.cvtColor(
                source,
                grayscale,
                Imgproc.COLOR_RGBA2GRAY
            )

            // Step 3: Invert image
            // Table borders and text become white,
            // background becomes black.
            Imgproc.threshold(
                grayscale,
                inverted,
                127.0,
                255.0,
                Imgproc.THRESH_BINARY_INV
            )

            // Step 4: Create horizontal kernel
            val horizontalLength = (source.cols() / 30).coerceAtLeast(20)

            horizontalKernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                Size(horizontalLength.toDouble(), 1.0)
            )

            // Step 5: Extract horizontal lines
            Imgproc.morphologyEx(
                inverted,
                horizontal,
                Imgproc.MORPH_OPEN,
                horizontalKernel
            )

            // Step 6: Create vertical kernel
            val verticalLength = (source.rows() / 50).coerceAtLeast(15)

            verticalKernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                Size(1.0, verticalLength.toDouble())
            )

            // Step 7: Extract vertical lines
            Imgproc.morphologyEx(
                inverted,
                vertical,
                Imgproc.MORPH_OPEN,
                verticalKernel
            )

            // Step 8: Combine horizontal and vertical lines
            org.opencv.core.Core.bitwise_or(
                horizontal,
                vertical,
                combined
            )

            // Step 9: Convert results into Android Bitmaps
            val horizontalBitmap = toBitmap(horizontal)
            val verticalBitmap = toBitmap(vertical)
            val combinedBitmap = toBitmap(combined)

            return GridDetectionResult(
                horizontalLines = horizontalBitmap,
                verticalLines = verticalBitmap,
                combinedLines = combinedBitmap
            )

        } finally {
            source.release()
            grayscale.release()
            inverted.release()
            horizontal.release()
            vertical.release()
            combined.release()
        }
    }

    private fun toBitmap(mat: Mat): Bitmap {

        val bitmap = Bitmap.createBitmap(
            mat.cols(),
            mat.rows(),
            Bitmap.Config.ARGB_8888
        )

        Utils.matToBitmap(mat, bitmap)

        return bitmap
    }
}
