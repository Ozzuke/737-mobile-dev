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
     * @param patientId Optional patient ID for clinicians viewing patient data
     * @return Result containing list of dataset summaries, or error
     */
    suspend fun getDatasets(patientId: String? = null): Result<List<DatasetSummary>>

    /**
     * Get dataset data for a specific preset
     * @param datasetId The dataset ID
     * @param preset The preset (24h, 7d, or 14d)
     * @param patientId Optional patient ID for clinicians viewing patient data
     * @return Result containing dataset data, or error
     */
    suspend fun getDatasetData(
        datasetId: String,
        preset: String,
        patientId: String? = null
    ): Result<DatasetData>

    /**
     * Analyze a dataset
     * @param datasetId The dataset ID
     * @param preset The preset (24h, 7d, or 14d)
     * @param lang The language code (default: en)
     * @param patientId Optional patient ID for clinicians viewing patient data
     * @return Result containing analysis results, or error
     */
    suspend fun analyzeDataset(
        datasetId: String,
        preset: String,
        lang: String = "en",
        patientId: String? = null
    ): Result<AnalysisResult>

    /**
     * Generate LLM explanation for a dataset
     * @param datasetId The dataset ID
     * @param preset The preset (24h, 7d, or 14d)
     * @param lang The language code (default: en)
     * @param style The explanation style (detailed, summary, or clinical)
     * @param patientId Optional patient ID for clinicians viewing patient data
     * @return Result containing LLM explanation, or error
     */
    suspend fun explainDataset(
        datasetId: String,
        preset: String,
        lang: String = "en",
        style: String = "detailed",
        patientId: String? = null
    ): Result<com.example.project.domain.model.LLMExplanation>

    /**
     * Upload a CGM dataset from a file URI
     * @param fileUri The URI of the file to upload
     * @param nickname An optional nickname for the dataset
     * @param unit The unit of the glucose readings (e.g., "mmol/L")
     * @param patientId Optional patient ID for clinicians uploading for a patient
     * @return Result containing the new dataset's summary, or error
     */
    suspend fun uploadDataset(
        fileUri: Uri,
        nickname: String?,
        unit: String?,
        patientId: String? = null
    ): Result<DatasetSummary>

    /**
     * Delete a dataset
     * @param datasetId The dataset ID to delete
     * @return Result containing true if successful, or error
     */
    suspend fun deleteDataset(datasetId: String): Result<Boolean>
}
