
package com.example.medexpertsystem.ui.preview

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfPreviewScreen(
    pdfUri: Uri,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(pdfUri) {
        loading = true
        error = null

        try {
            pages = withContext(Dispatchers.IO) {
                val descriptor = if (pdfUri.scheme == "file") {
                    android.os.ParcelFileDescriptor.open(
                        java.io.File(pdfUri.path!!),
                        android.os.ParcelFileDescriptor.MODE_READ_ONLY
                    )
                } else {
                    context.contentResolver.openFileDescriptor(pdfUri, "r")
                        ?: throw IllegalStateException("Unable to open PDF")
                }

                descriptor.use { parcel ->
                    PdfRenderer(parcel).use { renderer ->
                        (0 until renderer.pageCount).map { index ->
                            renderer.openPage(index).use { page ->
                                val scale = 2f
                                val bitmap = Bitmap.createBitmap(
                                    (page.width * scale).toInt(),
                                    (page.height * scale).toInt(),
                                    Bitmap.Config.ARGB_8888
                                )

                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                bitmap
                            }
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            error = exception.message ?: "Unable to render PDF"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PDF Report Preview",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> CircularProgressIndicator()

            error != null -> Text("PDF error: $error")

            pages.isNotEmpty() -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                pages.forEachIndexed { index, bitmap ->
                    Text("Page ${index + 1}")

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF page ${index + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            else -> Text("PDF contains no pages")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
