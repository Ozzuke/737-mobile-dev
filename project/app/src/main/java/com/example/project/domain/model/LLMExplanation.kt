package com.example.project.domain.model

/**
 * Domain model for LLM-generated explanations
 */
data class LLMExplanation(
    val summary: String,
    val interpretation: String,
    val recommendations: List<String>,
    val coveragePercent: Double?,
    val preset: String
)
