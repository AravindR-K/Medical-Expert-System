
package com.example.medexpertsystem.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medexpertsystem.ui.camera.CameraScreen
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
                    navController.navigate("camera")
                }
            )
        }

        composable("camera") {
            CameraScreen(
                onPhotoCaptured = { imageUri ->
                    navController.navigate(
                        "preview/${Uri.encode(imageUri.toString())}"
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "preview/{imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val imageUri = Uri.parse(
                Uri.decode(
                    backStackEntry.arguments?.getString("imageUri") ?: ""
                )
            )

            PreviewScreen(
                imageUri = imageUri,
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
