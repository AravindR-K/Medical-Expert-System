
package com.example.medexpertsystem

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.medexpertsystem.data.ocr.OpenCvManager
import com.example.medexpertsystem.navigation.NavGraph
import com.example.medexpertsystem.ui.theme.MedExpertSystemTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise OpenCV
        val openCvReady = OpenCvManager.initialize()

        if (openCvReady) {
            Log.i("OpenCV", "OpenCV initialised successfully")
        } else {
            Log.e("OpenCV", "OpenCV initialisation failed")
        }

        enableEdgeToEdge()

        setContent {
            MedExpertSystemTheme {
                NavGraph()
            }
        }
    }
}
