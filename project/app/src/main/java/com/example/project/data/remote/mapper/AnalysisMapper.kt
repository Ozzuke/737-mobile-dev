package com.example.project.data.remote.mapper

import com.example.project.data.remote.dto.*
import com.example.project.domain.model.*

/**
 * Mapper functions to convert analysis DTOs to domain models
 */

fun AnalyzeResponseDto.toDomain(): AnalysisResult {
    return AnalysisResult(
        unit = unit,
        requestedPreset = meta.requestedPreset,
        availableDays = meta.availableDays,
        coveragePercent = meta.coveragePercent,
        overallRating = overall.toDomain(),
        trends = annotations.trends.map { it.toDomain() },
        extrema = annotations.extrema.map { it.toDomain() },
        patterns = patterns.map { it.toDomain() },
        summary = text.summary,
        interpretation = text.interpretation,
        warnings = meta.warnings
    )
}

fun OverallRatingDto.toDomain(): OverallRating {
    return OverallRating(
        category = RatingCategory.fromString(category),
        score = score,
        reasons = reasons
    )
}

fun TrendAnnotationDto.toDomain(): TrendAnnotation {
    return TrendAnnotation(
        startMinute = startMinute,
        endMinute = endMinute,
        slopeMmolLPerHour = slopeMmolLPerHour,
        direction = TrendDirection.fromString(direction),
        exampleSpan = exampleSpan
    )
}

fun ExtremaAnnotationDto.toDomain(): ExtremaAnnotation {
    return ExtremaAnnotation(
        minute = minute,
        value = value,
        kind = ExtremaKind.fromString(kind)
    )
}

fun PatternDto.toDomain(): Pattern {
    return Pattern(
        key = key,
        name = name,
        severity = PatternSeverity.fromString(severity),
        summary = summary,
        instances = instances.map { it.toDomain() }
    )
}

fun PatternInstanceDto.toDomain(): PatternInstance {
    return PatternInstance(
        date = date,
        startMinute = startMinute,
        endMinute = endMinute
    )
}
