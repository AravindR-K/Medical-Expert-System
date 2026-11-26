
package com.example.medexpertsystem.data.ocr

// Represents the state of an individual measurement.
enum class MeasurementStatus {
    VALUE,
    MISSING,
    INELICITABLE,
    NOT_REPORTED
}

// Preserves both the extracted text and its numeric value.
data class NcsMeasurementValue(
    val rawText: String,
    val numericValue: Double?,
    val status: MeasurementStatus,
    val correctedText: String? = null
)

// One NCS measurement row.
data class NcsMeasurement(
    val segment: String,
    val latencyMs: NcsMeasurementValue,
    val amplitudeMv: NcsMeasurementValue,
    val durationMs: NcsMeasurementValue,
    val latencyDifference: NcsMeasurementValue,
    val distanceMm: NcsMeasurementValue,
    val conductionVelocity: NcsMeasurementValue
)

// One nerve section, for example Medianus Motor Left.
data class NcsSection(
    val sectionName: String,
    val measurements: List<NcsMeasurement>
)

// Complete reconstructed report.
data class NcsReport(
    val sections: List<NcsSection>
)

class NcsReportParser {

    fun parse(
        extractionResult: TableExtractionResult
    ): NcsReport {

        val sections = mutableListOf<NcsSection>()

        var currentSectionName: String? = null
        var currentMeasurements = mutableListOf<NcsMeasurement>()

        fun saveCurrentSection() {
            val name = currentSectionName ?: return

            sections.add(
                NcsSection(
                    sectionName = name,
                    measurements = currentMeasurements.toList()
                )
            )
        }

        for (row in extractionResult.rows) {

            val cells = row.cells.sortedBy { it.columnIndex }

            if (cells.isEmpty()) continue

            val firstCell = cells.firstOrNull {
                it.columnIndex == 0
            }?.text.orEmpty().trim().trim('|').trim()

            if (firstCell.isBlank()) continue

            // Ignore table headers and units.
            if (
                firstCell.equals("MNCS", ignoreCase = true) ||
                firstCell.equals("Nerve", ignoreCase = true)
            ) {
                continue
            }

            // Detect a nerve section heading.
            val isSectionHeading =
                firstCell.matches(
                    Regex(
                        ".*\\b(Left|Right)\\s*$",
                        RegexOption.IGNORE_CASE
                    )
                ) && cells.all {
                    it.columnIndex == 0 ||
                            it.text.isBlank()
                }

            if (isSectionHeading) {

                saveCurrentSection()

                currentSectionName = firstCell
                currentMeasurements = mutableListOf()

                continue
            }

            // Ignore measurement rows until a section is identified.
            if (currentSectionName == null) continue

            fun cellText(columnIndex: Int): String {
                return cells.firstOrNull {
                    it.columnIndex == columnIndex
                }?.text?.trim().orEmpty()
            }

            val segment = cellText(0).trim('|').trim()

            if (segment.isBlank()) continue

            currentMeasurements.add(
                NcsMeasurement(
                    segment = segment,
                    latencyMs = parseValue(cellText(1)),
                    amplitudeMv = parseValue(cellText(2)),
                    durationMs = parseValue(cellText(3)),
                    latencyDifference = parseValue(cellText(4)),
                    distanceMm = parseValue(cellText(5)),
                    conductionVelocity = parseValue(cellText(6))
                )
            )
        }

        saveCurrentSection()

        return NcsReport(
            sections = sections
        )
    }

    private fun parseValue(raw: String): NcsMeasurementValue {

        val text = raw.trim()

        if (text.isBlank()) {
            return NcsMeasurementValue(
                rawText = "",
                numericValue = null,
                status = MeasurementStatus.MISSING
            )
        }

        if (text.equals("Inelicitable", ignoreCase = true)) {
            return NcsMeasurementValue(
                rawText = text,
                numericValue = null,
                status = MeasurementStatus.INELICITABLE
            )
        }

        if (text == "--" || text == "—") {
            return NcsMeasurementValue(
                rawText = text,
                numericValue = null,
                status = MeasurementStatus.NOT_REPORTED
            )
        }

        val number = text.replace(",", "").toDoubleOrNull()

        if (number != null) {
            return NcsMeasurementValue(
                rawText = text,
                numericValue = number,
                status = MeasurementStatus.VALUE
            )
        }

        // Preserve unrecognised text rather than inventing a value.
        return NcsMeasurementValue(
            rawText = text,
            numericValue = null,
            status = MeasurementStatus.NOT_REPORTED
        )
    }
}
