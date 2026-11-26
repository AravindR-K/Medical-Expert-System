package com.example.medexpertsystem.data.ocr

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

data class ProcessedImage(
    val grayscale: Bitmap,
    val thresholded: Bitmap
)

class ImagePreprocessor {

    fun preprocess(originalBitmap: Bitmap): ProcessedImage {

        // Convert Android Bitmap into OpenCV Mat
        val source = Mat()
        Utils.bitmapToMat(originalBitmap, source)

        val grayscale = Mat()
        val blurred = Mat()
        val thresholded = Mat()

        try {
            // Step 1: Convert RGBA to grayscale
            Imgproc.cvtColor(
                source,
                grayscale,
                Imgproc.COLOR_RGBA2GRAY
            )

            // Step 2: Remove small image noise
            Imgproc.GaussianBlur(
                grayscale,
                blurred,
                Size(5.0, 5.0),
                0.0
            )

            // Step 3: Apply adaptive thresholding
            Imgproc.adaptiveThreshold(
                blurred,
                thresholded,
                255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                31,
                10.0
            )

            // Convert grayscale Mat to Bitmap
            val grayscaleBitmap = Bitmap.createBitmap(
                grayscale.cols(),
                grayscale.rows(),
                Bitmap.Config.ARGB_8888
            )

            Utils.matToBitmap(grayscale, grayscaleBitmap)

            // Convert thresholded Mat to Bitmap
            val thresholdBitmap = Bitmap.createBitmap(
                thresholded.cols(),
                thresholded.rows(),
                Bitmap.Config.ARGB_8888
            )

            Utils.matToBitmap(thresholded, thresholdBitmap)

            return ProcessedImage(
                grayscale = grayscaleBitmap,
                thresholded = thresholdBitmap
            )

        } finally {
            // Release native memory
            source.release()
            grayscale.release()
            blurred.release()
            thresholded.release()
        }
    }
}
