package com.example.project.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs for analysis-related API responses
 */

@JsonClass(generateAdapter = true)
data class AnalyzeResponseDto(
    @Json(name = "unit") val unit: String?,
    @Json(name = "analysis_period_start") val analysisPeriodStart: String?,
    @Json(name = "analysis_period_end") val analysisPeriodEnd: String?,
    @Json(name = "analysis_days") val analysisDays: Int?,
    @Json(name = "data_quality") val dataQuality: DataQualityReportDto?,
    @Json(name = "basal_patterns") val basalPatterns: List<BasalPatternDto>?,
    @Json(name = "physiological_patterns") val physiologicalPatterns: List<PhysiologicalPatternDto>?,
    @Json(name = "specific_events") val specificEvents: List<SpecificEventDto>?,
    @Json(name = "warnings") val warnings: List<String>?,
    @Json(name = "overall_assessment") val overallAssessment: OverallAssessmentDto?,
    @Json(name = "text") val text: AnalysisTextDto?
)

@JsonClass(generateAdapter = true)
data class OverallAssessmentDto(
    @Json(name = "status") val status: String?,
    @Json(name = "priority") val priority: Int?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "top_concerns") val topConcerns: List<String>?,
    @Json(name = "recommended_actions") val recommendedActions: List<String>?
)

@JsonClass(generateAdapter = true)
data class DataQualityReportDto(
    @Json(name = "overall_quality") val overallQuality: String?,
    @Json(name = "total_days") val totalDays: Int?,
    @Json(name = "valid_days") val validDays: Int?,
    @Json(name = "total_intervals") val totalIntervals: Int?,
    @Json(name = "valid_intervals") val validIntervals: Int?,
    @Json(name = "coverage_percentage") val coveragePercentage: Double?,
    @Json(name = "gap_issues") val gapIssues: List<String>?,
    @Json(name = "warnings") val warnings: List<String>?,
    @Json(name = "artifacts_removed") val artifactsRemoved: Map<String, Int>?
)

@JsonClass(generateAdapter = true)
data class BasalPatternDto(
    @Json(name = "interval_name") val intervalName: String?,
    @Json(name = "pattern_type") val patternType: String?,
    @Json(name = "direction") val direction: String?,
    @Json(name = "median_change_per_hour") val medianChangePerHour: Double?,
    @Json(name = "frequency") val frequency: Int?,
    @Json(name = "total_days_analyzed") val totalDaysAnalyzed: Int?,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class PhysiologicalPatternDto(
    @Json(name = "pattern_name") val patternName: String?,
    @Json(name = "time_window") val timeWindow: String?,
    @Json(name = "is_detected") val isDetected: Boolean?,
    @Json(name = "typical_rise_mmol") val typicalRiseMmol: Double?,
    @Json(name = "age_appropriate") val ageAppropriate: Boolean?,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class SpecificEventDto(
    @Json(name = "event_type") val eventType: String?,
    @Json(name = "timestamp") val timestamp: String?,
    @Json(name = "glucose_value") val glucoseValue: Double?,
    @Json(name = "duration_minutes") val durationMinutes: Double?,
    @Json(name = "severity") val severity: String?,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class AnalysisTextDto(
    @Json(name = "summary") val summary: String?,
    @Json(name = "interpretation") val interpretation: String?
)

@JsonClass(generateAdapter = true)
data class ExplainResponseDto(
    @Json(name = "explanation") val explanation: ExplanationContentDto?,
    @Json(name = "meta") val meta: ExplainMetaDto?
)

@JsonClass(generateAdapter = true)
data class ExplanationContentDto(
    @Json(name = "summary") val summary: String?,
    @Json(name = "interpretation") val interpretation: String?,
    @Json(name = "recommendations") val recommendations: List<String>?,
    @Json(name = "concerns") val concerns: List<String>?
)

@JsonClass(generateAdapter = true)
data class ExplainMetaDto(
    @Json(name = "coverage_percent") val coveragePercent: Double?,
    @Json(name = "preset") val preset: String?,
    @Json(name = "lang") val lang: String?,
    @Json(name = "style") val style: String?
)
