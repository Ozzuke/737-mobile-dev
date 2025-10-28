package com.example.project.data.remote.repository

import com.example.project.data.remote.api.CgmApiService
import com.example.project.data.remote.dto.AnalyzeRequestDto
import com.example.project.data.remote.mapper.toDomain
import com.example.project.domain.model.AnalysisResult
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.DatasetSummary
import com.example.project.domain.repository.CgmApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Implementation of CgmApiRepository
 * Handles API calls and converts responses to domain models
 */
class CgmApiRepositoryImpl(
    private val apiService: CgmApiService
) : CgmApiRepository {

    override suspend fun checkHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHealth()
            if (response.isSuccessful && response.body()?.status == "ok") {
                Result.success(true)
            } else {
                Result.failure(Exception("Health check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDatasets(): Result<List<DatasetSummary>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDatasets()
            if (response.isSuccessful) {
                val datasets = response.body()?.items?.map { it.toDomain() } ?: emptyList()
                Result.success(datasets)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDatasetData(
        datasetId: String,
        preset: String
    ): Result<DatasetData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDatasetData(datasetId, preset)
            if (response.isSuccessful) {
                val datasetData = response.body()?.toDomain()
                if (datasetData != null) {
                    Result.success(datasetData)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeDataset(
        datasetId: String,
        preset: String,
        lang: String
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val request = AnalyzeRequestDto(
                datasetId = datasetId,
                preset = preset,
                lang = lang
            )
            val response = apiService.analyzeDataset(request)
            if (response.isSuccessful) {
                val analysis = response.body()?.toDomain()
                if (analysis != null) {
                    Result.success(analysis)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract error message from HTTP response
     */
    private fun <T> getErrorMessage(response: Response<T>): String {
        return when (response.code()) {
            401 -> "Authentication failed. Please check your API key."
            404 -> "Resource not found."
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "Error ${response.code()}: ${response.message()}"
        }
    }
}
