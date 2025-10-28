package com.example.project.domain.repository

import android.net.Uri
import com.example.project.domain.model.AnalysisResult
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.DatasetSummary

/**
 * Repository interface for CGM API operations
 * Defines the contract for fetching data from the remote CGM API
 */
interface CgmApiRepository {
    /**
     * Check if the API is healthy
     * @return Result containing true if healthy, or error
     */
    suspend fun checkHealth(): Result<Boolean>

    /**
     * Get all datasets from the API
     * @return Result containing list of dataset summaries, or error
     */
    suspend fun getDatasets(): Result<List<DatasetSummary>>

    /**
     * Get dataset data for a specific preset
     * @param datasetId The dataset ID
     * @param preset The preset (24h, 7d, or 14d)
     * @return Result containing dataset data, or error
     */
    suspend fun getDatasetData(datasetId: String, preset: String): Result<DatasetData>

    /**
     * Analyze a dataset
     * @param datasetId The dataset ID
     * @param preset The preset (24h, 7d, or 14d)
     * @param lang The language code (default: en)
     * @return Result containing analysis results, or error
     */
    suspend fun analyzeDataset(
        datasetId: String,
        preset: String,
        lang: String = "en"
    ): Result<AnalysisResult>

    /**
     * Upload a CGM dataset from a file URI
     * @param fileUri The URI of the file to upload
     * @param nickname An optional nickname for the dataset
     * @param unit The unit of the glucose readings (e.g., "mmol/L")
     * @return Result containing the new dataset's summary, or error
     */
    suspend fun uploadDataset(
        fileUri: Uri,
        nickname: String?,
        unit: String?
    ): Result<DatasetSummary>
}
