package com.example.medexpertsystem.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medexpertsystem.ui.capture.CaptureReportScreen
import com.example.medexpertsystem.ui.preview.PreviewScreen
import com.example.medexpertsystem.ui.result.ResultScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "capture"
    ) {

        composable("capture") {
            CaptureReportScreen(
                onCaptureClick = {
                    navController.navigate("preview")
                }
            )
        }

        composable("preview") {
            PreviewScreen(
                onAnalyzeClick = {
                    navController.navigate("result")
                },
                onRetakeClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("result") {
            ResultScreen(
                onDoneClick = {
                    navController.popBackStack("capture", inclusive = false)
                }
            )
        }
    }
}
