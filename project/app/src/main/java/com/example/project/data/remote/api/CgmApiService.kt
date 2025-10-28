package com.example.project.data.remote.api

import com.example.project.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for the CGM Analyzer API
 * Base URL: http://cgm.cloud.ut.ee/api/v1
 */
interface CgmApiService {

    /**
     * Health check endpoint
     * GET /healthz
     */
    @GET("healthz")
    suspend fun getHealth(): Response<HealthResponseDto>

    /**
     * List all datasets
     * GET /datasets
     */
    @GET("datasets")
    suspend fun getDatasets(): Response<DatasetsResponseDto>

    /**
     * Get dataset data for a specific preset
     * GET /datasets/{dataset_id}?preset={preset}
     * @param datasetId The dataset ID
     * @param preset One of: 24h, 7d, 14d
     */
    @GET("datasets/{dataset_id}")
    suspend fun getDatasetData(
        @Path("dataset_id") datasetId: String,
        @Query("preset") preset: String
    ): Response<DatasetDataResponseDto>

    /**
     * Upload CGM CSV file
     * POST /datasets
     * @param file The CSV file to upload
     * @param unit Optional unit (mmol/L or mg/dL)
     * @param nickname Optional nickname for the dataset
     */
    @Multipart
    @POST("datasets")
    suspend fun uploadDataset(
        @Part file: MultipartBody.Part,
        @Part("unit") unit: RequestBody? = null,
        @Part("nickname") nickname: RequestBody? = null
    ): Response<UploadDatasetResponseDto>

    /**
     * Analyze a dataset
     * POST /analyze
     * @param request The analysis request body
     */
    @POST("analyze")
    suspend fun analyzeDataset(
        @Body request: AnalyzeRequestDto
    ): Response<AnalyzeResponseDto>
}
