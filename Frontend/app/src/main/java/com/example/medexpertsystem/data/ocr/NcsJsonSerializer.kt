
package com.example.medexpertsystem.data.ocr

import org.json.JSONArray
import org.json.JSONObject

class NcsJsonSerializer {

    fun serialize(report: NcsReport): String {

        val root = JSONObject()
        val sectionsArray = JSONArray()

        for (section in report.sections) {

            val sectionObject = JSONObject()
            val measurementsArray = JSONArray()

            sectionObject.put("sectionName", section.sectionName)

            for (measurement in section.measurements) {

                val measurementObject = JSONObject()

                measurementObject.put("segment", measurement.segment)

                measurementObject.put(
                    "latency_ms",
                    measurementValueToJson(measurement.latencyMs)
                )

                measurementObject.put(
                    "amplitude_mv",
                    measurementValueToJson(measurement.amplitudeMv)
                )

                measurementObject.put(
                    "duration_ms",
                    measurementValueToJson(measurement.durationMs)
                )

                measurementObject.put(
                    "latency_difference",
                    measurementValueToJson(measurement.latencyDifference)
                )

                measurementObject.put(
                    "distance_mm",
                    measurementValueToJson(measurement.distanceMm)
                )

                measurementObject.put(
                    "conduction_velocity",
                    measurementValueToJson(measurement.conductionVelocity)
                )

                measurementsArray.put(measurementObject)
            }

            sectionObject.put("measurements", measurementsArray)

            sectionsArray.put(sectionObject)
        }

        root.put("sections", sectionsArray)

        return root.toString(2)
    }


    private fun measurementValueToJson(
        measurement: NcsMeasurementValue
    ): JSONObject {

        val valueObject = JSONObject()

        // Current numeric value after correction, if any
        valueObject.put(
            "value",
            measurement.numericValue ?: JSONObject.NULL
        )

        // Measurement status
        valueObject.put(
            "status",
            measurement.status.name
        )

        // Original OCR-extracted text
        valueObject.put(
            "rawText",
            measurement.rawText
        )

        // User-corrected text, null if not edited
        valueObject.put(
            "correctedText",
            measurement.correctedText ?: JSONObject.NULL
        )

        return valueObject
    }

}
