package com.example.medexpertsystem.data.ocr

import org.opencv.android.OpenCVLoader

object OpenCvManager {

    @Volatile
    private var isInitialized = false

    @Synchronized
    fun initialize(): Boolean {

        if (isInitialized) {
            return true
        }

        isInitialized = OpenCVLoader.initLocal()

        return isInitialized
    }
}
