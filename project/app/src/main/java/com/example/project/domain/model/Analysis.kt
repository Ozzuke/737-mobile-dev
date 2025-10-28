package com.example.project.domain.model

/**
 * Domain models for analysis results
 */

data class AnalysisResult(
    val unit: String,
    val requestedPreset: String,
    val availableDays: Int,
    val coveragePercent: Double,
    val overallRating: OverallRating,
    val trends: List<TrendAnnotation>,
    val extrema: List<ExtremaAnnotation>,
    val patterns: List<Pattern>,
    val summary: String,
    val interpretation: String,
    val warnings: List<String>
)

data class OverallRating(
    val category: RatingCategory,
    val score: Double?,
    val reasons: List<String>
)

enum class RatingCategory {
    GOOD,
    ATTENTION,
    URGENT,
    UNKNOWN;

    companion object {
        fun fromString(value: String): RatingCategory {
            return when (value.lowercase()) {
                "good" -> GOOD
                "attention" -> ATTENTION
                "urgent" -> URGENT
                else -> UNKNOWN
            }
        }
    }
}

data class TrendAnnotation(
    val startMinute: Int,
    val endMinute: Int,
    val slopeMmolLPerHour: Double,
    val direction: TrendDirection,
    val exampleSpan: String
)

enum class TrendDirection {
    UP,
    DOWN,
    UNKNOWN;

    companion object {
        fun fromString(value: String): TrendDirection {
            return when (value.lowercase()) {
                "up" -> UP
                "down" -> DOWN
                else -> UNKNOWN
            }
        }
    }
}

data class ExtremaAnnotation(
    val minute: Int,
    val value: Double,
    val kind: ExtremaKind
)

enum class ExtremaKind {
    MAX,
    MIN,
    UNKNOWN;

    companion object {
        fun fromString(value: String): ExtremaKind {
            return when (value.lowercase()) {
                "max" -> MAX
                "min" -> MIN
                else -> UNKNOWN
            }
        }
    }
}

data class Pattern(
    val key: String,
    val name: String,
    val severity: PatternSeverity,
    val summary: String,
    val instances: List<PatternInstance>
)

enum class PatternSeverity {
    INFO,
    MODERATE,
    HIGH,
    UNKNOWN;

    companion object {
        fun fromString(value: String): PatternSeverity {
            return when (value.lowercase()) {
                "info" -> INFO
                "moderate" -> MODERATE
                "high" -> HIGH
                else -> UNKNOWN
            }
        }
    }
}

data class PatternInstance(
    val date: String,
    val startMinute: Int,
    val endMinute: Int
)
