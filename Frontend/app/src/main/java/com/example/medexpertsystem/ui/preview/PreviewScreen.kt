
package com.example.medexpertsystem.ui.preview

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

@Composable
fun PreviewScreen(
    imageUri: Uri,
    onAnalyzeClick: () -> Unit,
    onRetakeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Preview Report",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        AsyncImage(
            model = imageUri,
            contentDescription = "Captured medical report",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Report")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRetakeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retake")
        }
    }
}
