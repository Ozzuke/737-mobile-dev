package com.example.project.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs for analysis-related API responses
 * Note: Main response DTOs (AnalyzeResponse, ExplainResponse) are now in CgmApiService.kt
 * This file contains shared component DTOs used across multiple endpoints
 */

@JsonClass(generateAdapter = true)
data class AnnotationsDto(
    @Json(name = "trends")
    val trends: List<TrendAnnotationDto>,
    @Json(name = "extrema")
    val extrema: List<ExtremaAnnotationDto>
)

@JsonClass(generateAdapter = true)
data class TrendAnnotationDto(
    @Json(name = "start_minute")
    val startMinute: Int,
    @Json(name = "end_minute")
    val endMinute: Int,
    @Json(name = "slope_mmol_l_per_hour")
    val slopeMmolLPerHour: Double,
    @Json(name = "direction")
    val direction: String,
    @Json(name = "example_span")
    val exampleSpan: String
)

@JsonClass(generateAdapter = true)
data class ExtremaAnnotationDto(
    @Json(name = "minute")
    val minute: Int,
    @Json(name = "value")
    val value: Double,
    @Json(name = "kind")
    val kind: String
)

@JsonClass(generateAdapter = true)
data class PatternDto(
    @Json(name = "key")
    val key: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "severity")
    val severity: String,
    @Json(name = "summary")
    val summary: String,
    @Json(name = "instances")
    val instances: List<PatternInstanceDto>
)

@JsonClass(generateAdapter = true)
data class PatternInstanceDto(
    @Json(name = "date")
    val date: String,
    @Json(name = "start_minute")
    val startMinute: Int,
    @Json(name = "end_minute")
    val endMinute: Int
)

@JsonClass(generateAdapter = true)
data class AnalysisTextDto(
    @Json(name = "summary")
    val summary: String,
    @Json(name = "interpretation")
    val interpretation: String
)

@JsonClass(generateAdapter = true)
data class ExplainMetaDto(
    @Json(name = "coverage_percent")
    val coveragePercent: Double?,
    @Json(name = "preset")
    val preset: String,
    @Json(name = "lang")
    val lang: String,
    @Json(name = "style")
    val style: String
)
