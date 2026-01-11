package com.example.medexpertsystem.ui.capture

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CaptureReportScreen(
    onCaptureClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "MedExpert System",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Capture radiology report",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onCaptureClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Capture Report")
        }
    }
}
