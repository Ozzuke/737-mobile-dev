package com.example.project.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs (Data Transfer Objects) for dataset-related API responses
 * Based on the OpenAPI specification at /api/v1
 */

@JsonClass(generateAdapter = true)
data class DatasetListResponseDto(
    @Json(name = "items")
    val items: List<DatasetItemDto>
)

@JsonClass(generateAdapter = true)
data class DatasetItemDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "patient_id") val patientId: String?,
    @Json(name = "unit") val unit: String?,
    @Json(name = "total_readings") val totalReadings: Int?,
    @Json(name = "date_range") val dateRange: DateRangeDto?,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DataOverlayResponseDto(
    @Json(name = "dataset") val dataset: DatasetMetadataDto?,
    @Json(name = "unit") val unit: String?,
    @Json(name = "meta") val meta: OverlayMetaDto?,
    @Json(name = "overlay") val overlay: OverlayDataDto?
)

@JsonClass(generateAdapter = true)
data class DatasetMetadataDto(
    @Json(name = "dataset_id") val datasetId: String?,
    @Json(name = "patient_id") val patientId: String?,
    @Json(name = "unit_internal") val unitInternal: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class OverlayMetaDto(
    @Json(name = "coverage_percent") val coveragePercent: Double?,
    @Json(name = "preset") val preset: String?
)

@JsonClass(generateAdapter = true)
data class OverlayDataDto(
    @Json(name = "days") val days: List<OverlayDayDto>?
)

@JsonClass(generateAdapter = true)
data class OverlayDayDto(
    @Json(name = "date") val date: String,
    @Json(name = "points") val points: List<OverlayPointDto>
)

@JsonClass(generateAdapter = true)
data class OverlayPointDto(
    @Json(name = "minute") val minute: Int,
    @Json(name = "glucose") val glucose: Double
)

@JsonClass(generateAdapter = true)
data class UploadDataResponseDto(
    @Json(name = "dataset_id") val datasetId: String?,
    @Json(name = "readings_added") val readingsAdded: Int?,
    @Json(name = "readings_updated") val readingsUpdated: Int?,
    @Json(name = "total_readings") val totalReadings: Int?,
    @Json(name = "date_range") val dateRange: DateRangeDto?,
    @Json(name = "unit") val unit: String?
)

@JsonClass(generateAdapter = true)
data class DateRangeDto(
    @Json(name = "start") val start: String? = null,
    @Json(name = "end") val end: String? = null
)
