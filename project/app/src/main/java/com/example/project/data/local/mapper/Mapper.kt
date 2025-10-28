package com.example.project.data.local.mapper

import com.example.project.data.local.entity.GlucoseReadingEntity
import com.example.project.domain.model.GlucoseReading

fun GlucoseReadingEntity.toDomainModel(): GlucoseReading {
    return GlucoseReading(
        id = this.id,
        timestamp = this.timestamp,
        glucoseValue = this.glucoseValue
    )
}

fun GlucoseReading.toEntity(): GlucoseReadingEntity {
    return GlucoseReadingEntity(
        id = this.id,
        timestamp = this.timestamp,
        glucoseValue = this.glucoseValue
    )
}
