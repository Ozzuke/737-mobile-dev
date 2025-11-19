package com.example.project.data.remote.mapper

/**
 * Mapper functions to convert dataset DTOs to domain models
 *
 * Note: Dataset mapping logic is now handled directly in CgmApiRepositoryImpl
 * as the API response structure changed. The new API returns:
 * - DatasetListResponseDto with DatasetItemDto items (for dataset lists)
 * - DataOverlayResponseDto with OverlayDataDto (for overlay data)
 *
 * Mapping is done inline in the repository to handle the complex data transformations
 * (e.g., calculating sampling intervals, mapping nested structures).
 *
 * This file is kept for potential future mapper functions if needed.
 */

