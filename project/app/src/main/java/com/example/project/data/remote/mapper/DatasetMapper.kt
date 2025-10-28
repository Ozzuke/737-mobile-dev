package com.example.project.data.remote.mapper

import com.example.project.data.remote.dto.*
import com.example.project.domain.model.*

/**
 * Mapper functions to convert dataset DTOs to domain models
 */

fun DatasetSummaryDto.toDomain(): DatasetSummary {
    return DatasetSummary(
        datasetId = datasetId,
        nickname = nickname ?: "Unnamed Dataset",
        createdAt = createdAt,
        rowCount = rowCount,
        startDate = start,
        endDate = end,
        unit = unitInternal,
        samplingIntervalMin = samplingIntervalMin
    )
}

fun DatasetDataResponseDto.toDomain(): DatasetData {
    return DatasetData(
        datasetId = dataset.datasetId,
        nickname = dataset.nickname ?: "Unnamed Dataset",
        unit = unit,
        requestedPreset = meta.requestedPreset,
        availableDays = meta.availableDays,
        coveragePercent = meta.coveragePercent,
        resolutionMin = meta.resolutionMin,
        warnings = meta.warnings,
        overlayDays = overlay.days.map { it.toDomain() }
    )
}

fun OverlayDayDto.toDomain(): OverlayDay {
    return OverlayDay(
        date = date,
        points = points.map { it.toDomain() }
    )
}

fun OverlayPointDto.toDomain(): GlucosePoint {
    return GlucosePoint(
        minute = minute,
        glucose = glucose
    )
}
