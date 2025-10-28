package com.example.project.domain.model

data class GlucoseReading(
    val id: Long = 0,
    val timestamp: String,
    val glucoseValue: Double
)