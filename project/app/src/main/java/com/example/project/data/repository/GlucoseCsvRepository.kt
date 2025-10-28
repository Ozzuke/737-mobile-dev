package com.example.project.data.repository

import android.content.Context
import android.net.Uri
import com.example.project.domain.model.GlucoseReading
import com.opencsv.CSVReader
import java.io.InputStreamReader

import com.example.project.domain.repository.GlucoseCsvRepository

class GlucoseCsvRepository : GlucoseCsvRepository {

    override fun parseGlucoseData(context: Context, uri: Uri): List<GlucoseReading> {
        val readings = mutableListOf<GlucoseReading>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                CSVReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readAll().forEach { line ->
                        // Skip metadata rows and headers
                        // Only process rows where Event Type is "EGV" (Estimated Glucose Value)
                        if (line.size >= 8 && line[2].trim('"') == "EGV") {
                            val timestamp = line[1].trim('"')
                            val glucoseStr = line[7].trim('"')

                            if (timestamp.isNotEmpty() && glucoseStr.isNotEmpty()) {
                                try {
                                    readings.add(
                                        GlucoseReading(
                                            timestamp = timestamp,
                                            glucoseValue = glucoseStr.toDouble()
                                        )
                                    )
                                } catch (e: NumberFormatException) {
                                    // Skip invalid glucose values
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return readings
    }
}