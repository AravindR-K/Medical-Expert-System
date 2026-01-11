package com.example.medexpertsystem.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultScreen(
    onDoneClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Analysis Result",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Diagnosis",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Suspected post-ganglionic brachial plexopathy",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recommendations",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = """
• Correlate clinically
• MRI brachial plexus advised
• Early physiotherapy recommended
• Neurology follow-up suggested
            """.trimIndent(),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}
