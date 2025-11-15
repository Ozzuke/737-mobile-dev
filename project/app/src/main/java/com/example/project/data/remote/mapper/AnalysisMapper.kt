package com.example.project.data.remote.mapper

import com.example.project.data.remote.dto.*
import com.example.project.domain.model.*

/**
 * Mapper functions to convert analysis DTOs to domain models
 * Note: Main conversion logic is now handled directly in CgmApiRepositoryImpl
 * These mappers are kept for shared component conversions
 */

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
