package com.example.project.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_readings")
data class GlucoseReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String,
    val glucoseValue: Double
)