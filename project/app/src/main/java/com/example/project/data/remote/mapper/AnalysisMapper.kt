package com.example.project.data.remote.mapper

/**
 * Mapper functions to convert analysis DTOs to domain models
 *
 * Note: Analysis mapping logic is now handled directly in CgmApiRepositoryImpl
 * as the API response structure changed. The new AnalyzeResponseDto structure
 * (with basalPatterns, physiologicalPatterns, specificEvents) is mapped inline
 * to the domain AnalysisResult model.
 *
 * This file is kept for potential future mapper functions if needed.
 */

