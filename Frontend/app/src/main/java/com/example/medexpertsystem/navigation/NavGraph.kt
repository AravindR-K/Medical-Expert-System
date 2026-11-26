
package com.example.medexpertsystem.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medexpertsystem.data.ocr.OcrProcessor
import com.example.medexpertsystem.ui.camera.CameraScreen
import com.example.medexpertsystem.ui.capture.CaptureReportScreen
import com.example.medexpertsystem.ui.ocr.OcrResultScreen
import com.example.medexpertsystem.ui.preview.PdfPreviewScreen
import com.example.medexpertsystem.ui.preview.PreviewScreen
import com.example.medexpertsystem.ui.result.ResultScreen
import com.example.medexpertsystem.data.ocr.OcrBlock
import com.example.medexpertsystem.ui.preview.ImageProcessingTestScreen
import com.example.medexpertsystem.data.ocr.NcsReport
import com.example.medexpertsystem.data.ocr.NcsReportProcessingPipeline

@Composable
fun NavGraph() {

    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "capture"
    ) {

        composable("imageProcessingTest") {
            ImageProcessingTestScreen()
        }

        // Home / Capture screen
        composable("capture") {
            CaptureReportScreen(
                onCameraClick = {
                    navController.navigate("camera")
                },
                onGalleryImageSelected = { imageUri ->
                    navController.navigate(
                        "preview/${Uri.encode(imageUri.toString())}"
                    )
                },
                onPdfSelected = { pdfUri ->
                    navController.navigate(
                        "pdfPreview/${Uri.encode(pdfUri.toString())}"
                    )
                }
            )
        }

        // Camera screen
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

        // OCR screen
        composable(
            route = "ocr/{imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val imageUri = Uri.parse(
                Uri.decode(
                    entry.arguments?.getString("imageUri") ?: ""
                )
            )

            var extractedText by remember {
                mutableStateOf("")
            }

            var ocrBlocks by remember {
                mutableStateOf(emptyList<OcrBlock>())
            }

            var isLoading by remember {
                mutableStateOf(true)
            }

            var error by remember {
                mutableStateOf<String?>(null)
            }

            var ncsReport by remember {
                mutableStateOf<NcsReport?>(null)
            }

            var ncsJson by remember {
                mutableStateOf<String?>(null)
            }

            LaunchedEffect(imageUri) {
                try {
                    isLoading = true
                    error = null

                    val result = NcsReportProcessingPipeline().process(
                        context = context,
                        imageUri = imageUri
                    )

                    extractedText = result.ocrResult.fullText
                    ocrBlocks = result.ocrResult.blocks
                    ncsReport = result.ncsReport
                    ncsJson = result.json

                } catch (exception: Exception) {
                    error = exception.message ?: "NCS processing failed"
                } finally {
                    isLoading = false
                }
            }

            OcrResultScreen(
                extractedText = extractedText,
                isLoading = isLoading,
                error = error,
                onBackClick = {
                    navController.popBackStack()
                },
                ncsReport = ncsReport,
                ncsJson = ncsJson
            )
        }

        // Image preview screen
        composable(
            route = "preview/{imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val imageUri = Uri.parse(
                Uri.decode(
                    entry.arguments?.getString("imageUri") ?: ""
                )
            )

            PreviewScreen(
                imageUri = imageUri,
                onAnalyzeClick = {
                    navController.navigate(
                        "ocr/${Uri.encode(imageUri.toString())}"
                    )
                },
                onRetakeClick = {
                    navController.popBackStack()
                }
            )
        }

        // PDF preview screen
        composable(
            route = "pdfPreview/{pdfUri}",
            arguments = listOf(
                navArgument("pdfUri") {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val pdfUri = Uri.parse(
                Uri.decode(
                    entry.arguments?.getString("pdfUri") ?: ""
                )
            )

            PdfPreviewScreen(
                pdfUri = pdfUri,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Existing result screen
        composable("result") {
            ResultScreen(
                onDoneClick = {
                    navController.popBackStack(
                        "capture",
                        inclusive = false
                    )
                }
            )
        }
    }
}
