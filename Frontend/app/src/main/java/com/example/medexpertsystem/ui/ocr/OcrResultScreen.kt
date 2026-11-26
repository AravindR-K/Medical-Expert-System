
package com.example.medexpertsystem.ui.ocr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medexpertsystem.data.ocr.NcsReport
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import com.example.medexpertsystem.data.ocr.NcsMeasurement
import com.example.medexpertsystem.data.ocr.NcsMeasurementValue
import com.example.medexpertsystem.data.ocr.MeasurementStatus
import com.example.medexpertsystem.data.ocr.NcsJsonSerializer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager

@Composable
fun OcrResultScreen(
    extractedText: String,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    ncsReport: NcsReport?,
    ncsJson: String?
) {
    var editableReport by remember(ncsReport) {
        mutableStateOf(ncsReport)
    }
    var isSaved by remember(ncsReport) {
        mutableStateOf(false)
    }

    val clipboardManager = LocalClipboardManager.current

    val editableJson = remember(editableReport) {
        editableReport?.let {
            NcsJsonSerializer().serialize(it)
        }
    }


    // STEP 3: Update function
    fun updateMeasurement(
        sectionIndex: Int,
        measurementIndex: Int,
        update: (NcsMeasurement) -> NcsMeasurement
    ) {
        editableReport = editableReport?.let { report ->

            val updatedSections = report.sections.toMutableList()

            val section = updatedSections[sectionIndex]
            val updatedMeasurements = section.measurements.toMutableList()

            updatedMeasurements[measurementIndex] =
                update(updatedMeasurements[measurementIndex])

            updatedSections[sectionIndex] =
                section.copy(measurements = updatedMeasurements)

            report.copy(sections = updatedSections)
        }
    }
    // STEP 4: Your existing UI starts here
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "NCS Report Results",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Processing NCS report...")
            }

            error != null -> {
                Text(
                    text = "Processing failed: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            ncsReport != null -> {

                Text(
                    text = "Detected Sections: ${ncsReport.sections.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                editableReport?.sections?.forEachIndexed { sectionIndex, section ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                text = section.sectionName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            section.measurements.forEachIndexed { measurementIndex, measurement ->

                                HorizontalDivider()

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = measurement.segment,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                MeasurementRow(
                                    label = "Latency (ms)",
                                    value = measurement.latencyMs.correctedText
                                        ?: measurement.latencyMs.rawText,
                                    status = measurement.latencyMs.status.name,
                                    enabled = !isSaved,
                                    onValueChange = { newValue ->
                                        updateMeasurement(sectionIndex, measurementIndex) {
                                            it.copy(
                                                latencyMs = updateMeasurementValue(
                                                    it.latencyMs,
                                                    newValue
                                                )
                                            )
                                        }
                                    }
                                )
                                MeasurementRow(
                                    label = "Amplitude (mV)",
                                    value = measurement.amplitudeMv.correctedText
                                        ?: measurement.amplitudeMv.rawText,
                                    status = measurement.amplitudeMv.status.name,
                                    enabled = !isSaved,
                                    onValueChange = { newValue ->
                                        updateMeasurement(sectionIndex, measurementIndex) {
                                            it.copy(
                                                amplitudeMv = updateMeasurementValue(
                                                    it.amplitudeMv,
                                                    newValue
                                                )
                                            )
                                        }
                                    }
                                )

                                MeasurementRow(
                                    label = "Duration (ms)",
                                    value = measurement.durationMs.correctedText
                                        ?: measurement.durationMs.rawText,
                                    status = measurement.durationMs.status.name,
                                    enabled = !isSaved,
                                    onValueChange = { newValue ->
                                        updateMeasurement(sectionIndex, measurementIndex) {
                                            it.copy(
                                                durationMs = updateMeasurementValue(
                                                    it.durationMs,
                                                    newValue
                                                )
                                            )
                                        }
                                    }
                                )

                                MeasurementRow(
                                    label = "Latency Difference",
                                    value = measurement.latencyDifference.correctedText
                                        ?: measurement.latencyDifference.rawText,
                                    status = measurement.latencyDifference.status.name,
                                    enabled = !isSaved,
                                    onValueChange = { newValue ->
                                        updateMeasurement(sectionIndex, measurementIndex) {
                                            it.copy(
                                                latencyDifference = updateMeasurementValue(
                                                    it.latencyDifference,
                                                    newValue
                                                )
                                            )
                                        }
                                    }
                                )

                                MeasurementRow(
                                    label = "Distance (mm)",
                                    value = measurement.distanceMm.correctedText
                                        ?: measurement.distanceMm.rawText,
                                    status = measurement.distanceMm.status.name,
                                    enabled = !isSaved,
                                    onValueChange = { newValue ->
                                        updateMeasurement(sectionIndex, measurementIndex) {
                                            it.copy(
                                                distanceMm = updateMeasurementValue(
                                                    it.distanceMm,
                                                    newValue
                                                )
                                            )
                                        }
                                    }
                                )

                                MeasurementRow(
                                    label = "Conduction Velocity (m/s)",
                                    value = measurement.conductionVelocity.correctedText
                                        ?: measurement.conductionVelocity.rawText,
                                    status = measurement.conductionVelocity.status.name,
                                    enabled = !isSaved,
                                    onValueChange = { newValue ->
                                        updateMeasurement(sectionIndex, measurementIndex) {
                                            it.copy(
                                                conductionVelocity = updateMeasurementValue(
                                                    it.conductionVelocity,
                                                    newValue
                                                )
                                            )
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Generated JSON",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = editableJson ?: "JSON unavailable",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        editableJson?.let { json ->
                            clipboardManager.setText(
                                AnnotatedString(json)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = editableJson != null
                ) {
                    Text("Copy JSON")
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isSaved = !isSaved
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isSaved) "Edit Again" else "Save Corrections"
                    )
                }

                if (isSaved) {
                    Text(
                        text = "Report reviewed by user. Clinical validation pending.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }

            else -> {
                Text("No NCS report was generated.")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

private fun updateMeasurementValue(
    original: NcsMeasurementValue,
    newText: String
): NcsMeasurementValue {

    val text = newText.trim()

    // Empty input
    if (text.isBlank()) {
        return original.copy(
            correctedText = newText,
            numericValue = null,
            status = MeasurementStatus.MISSING
        )
    }

    // Inelicitable measurement
    if (text.equals("Inelicitable", ignoreCase = true)) {
        return original.copy(
            correctedText = newText,
            numericValue = null,
            status = MeasurementStatus.INELICITABLE
        )
    }

    // Not reported measurement
    if (text == "--" || text == "—") {
        return original.copy(
            correctedText = newText,
            numericValue = null,
            status = MeasurementStatus.NOT_REPORTED
        )
    }

    // Numeric validation
    val number = text.replace(",", "").toDoubleOrNull()

    if (number != null && number.isFinite() && number >= 0) {
        return original.copy(
            correctedText = newText,
            numericValue = number,
            status = MeasurementStatus.VALUE
        )
    }

    // Invalid input: preserve the user's entry but don't
    // interpret it as a valid numeric measurement.
    return original.copy(
        correctedText = newText,
        numericValue = null,
        status = MeasurementStatus.NOT_REPORTED
    )
}

@Composable
private fun MeasurementRow(
    label: String,
    value: String,
    status: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}