package com.example.project.domain.repository

import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.Flow

interface GlucoseRepository {
    val allReadings: Flow<List<GlucoseReading>>
    val latestReading: Flow<GlucoseReading?>
    suspend fun insertReadings(readings: List<GlucoseReading>)
    suspend fun deleteAllReadings()
}
