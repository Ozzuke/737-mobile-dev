package com.example.project.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs (Data Transfer Objects) for dataset-related API responses
 * Based on the OpenAPI specification at /api/v1
 */

@JsonClass(generateAdapter = true)
data class DatasetSummaryDto(
    @Json(name = "dataset_id")
    val datasetId: String,
    @Json(name = "nickname")
    val nickname: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "row_count")
    val rowCount: Int,
    @Json(name = "start")
    val start: String,
    @Json(name = "end")
    val end: String,
    @Json(name = "unit_internal")
    val unitInternal: String,
    @Json(name = "sampling_interval_min")
    val samplingIntervalMin: Int
)

@JsonClass(generateAdapter = true)
data class DatasetsResponseDto(
    @Json(name = "items")
    val items: List<DatasetSummaryDto>
)

@JsonClass(generateAdapter = true)
data class DatasetDataResponseDto(
    @Json(name = "dataset")
    val dataset: DatasetInfoDto,
    @Json(name = "unit")
    val unit: String,
    @Json(name = "meta")
    val meta: DataMetaDto,
    @Json(name = "overlay")
    val overlay: OverlayDto
)

@JsonClass(generateAdapter = true)
data class DatasetInfoDto(
    @Json(name = "dataset_id")
    val datasetId: String,
    @Json(name = "nickname")
    val nickname: String
)

@JsonClass(generateAdapter = true)
data class DataMetaDto(
    @Json(name = "requested_preset")
    val requestedPreset: String,
    @Json(name = "available_days")
    val availableDays: Int,
    @Json(name = "coverage_percent")
    val coveragePercent: Double,
    @Json(name = "resolution_min")
    val resolutionMin: Int,
    @Json(name = "warnings")
    val warnings: List<String>
)

@JsonClass(generateAdapter = true)
data class OverlayDto(
    @Json(name = "days")
    val days: List<OverlayDayDto>
)

@JsonClass(generateAdapter = true)
data class OverlayDayDto(
    @Json(name = "date")
    val date: String,
    @Json(name = "points")
    val points: List<OverlayPointDto>
)

@JsonClass(generateAdapter = true)
data class OverlayPointDto(
    @Json(name = "minute")
    val minute: Int,
    @Json(name = "glucose")
    val glucose: Double
)

@JsonClass(generateAdapter = true)
data class UploadDatasetResponseDto(
    @Json(name = "dataset_id")
    val datasetId: String,
    @Json(name = "validation")
    val validation: ValidationResultDto
)

@JsonClass(generateAdapter = true)
data class ValidationResultDto(
    @Json(name = "status")
    val status: String,
    @Json(name = "messages")
    val messages: List<String>,
    @Json(name = "detected_unit")
    val detectedUnit: String,
    @Json(name = "detected_format")
    val detectedFormat: String,
    @Json(name = "row_count")
    val rowCount: Int,
    @Json(name = "time_range")
    val timeRange: TimeRangeDto
)

@JsonClass(generateAdapter = true)
data class TimeRangeDto(
    @Json(name = "start")
    val start: String,
    @Json(name = "end")
    val end: String
)
