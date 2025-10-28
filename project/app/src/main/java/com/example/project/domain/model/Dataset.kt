package com.example.project.domain.model

/**
 * Domain models for dataset information
 * These are UI-friendly representations of the API data
 */

data class DatasetSummary(
    val datasetId: String,
    val nickname: String,
    val createdAt: String,
    val rowCount: Int,
    val startDate: String,
    val endDate: String,
    val unit: String,
    val samplingIntervalMin: Int
)

data class DatasetData(
    val datasetId: String,
    val nickname: String,
    val unit: String,
    val requestedPreset: String,
    val availableDays: Int,
    val coveragePercent: Double,
    val resolutionMin: Int,
    val warnings: List<String>,
    val overlayDays: List<OverlayDay>
)

data class OverlayDay(
    val date: String,
    val points: List<GlucosePoint>
)

data class GlucosePoint(
    val minute: Int,
    val glucose: Double
)
