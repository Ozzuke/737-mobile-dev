package com.example.project.data.remote.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.project.data.remote.api.CgmApiService
import com.example.project.data.remote.dto.AnalyzeRequestDto
import com.example.project.data.remote.mapper.toDomain
import com.example.project.domain.model.AnalysisResult
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.DatasetSummary
import com.example.project.domain.repository.CgmApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

/**
 * Implementation of CgmApiRepository
 * Handles API calls and converts responses to domain models
 */
class CgmApiRepositoryImpl(
    private val apiService: CgmApiService,
    private val context: Context
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

    override suspend fun uploadDataset(
        fileUri: Uri,
        nickname: String?,
        unit: String?
    ): Result<DatasetSummary> = withContext(Dispatchers.IO) {
        try {
            val tempFile = createTempFileFromUri(fileUri)
                ?: return@withContext Result.failure(Exception("Failed to create temporary file from URI"))

            // Create the file part with proper content type
            // The field name MUST be "cgm_csv" to match the API expectation
            val requestFile = tempFile.asRequestBody("text/csv".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("cgm_csv", tempFile.name, requestFile)

            // Only create optional parts if values are provided
            val nicknamePart = nickname?.let {
                it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            val unitPart = unit?.let {
                it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            val response = apiService.uploadDataset(filePart, unitPart, nicknamePart)

            tempFile.delete()

            if (response.isSuccessful && response.body() != null) {
                val datasetId = response.body()!!.datasetId
                // The upload response is minimal, so we fetch the full list again
                // and find the newly created dataset to return its summary.
                getDatasets().map { datasets ->
                    datasets.first { it.datasetId == datasetId }
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val stream = context.contentResolver.openInputStream(uri)
            val fileName = getFileName(uri)
            val tempFile = File(context.cacheDir, fileName)
            tempFile.createNewFile()
            stream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (colIndex >= 0) {
                        result = cursor.getString(colIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "upload.csv"
    }

    /**
     * Extract error message from HTTP response
     */
    private fun <T> getErrorMessage(response: Response<T>): String {
        return when (response.code()) {
            401 -> "Authentication failed. Please check your API key."
            404 -> "Resource not found."
            422 -> {
                // Try to get detailed error from response body
                val errorBody = response.errorBody()?.string()
                "Validation error (422): ${errorBody ?: response.message()}"
            }
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "Error ${response.code()}: ${response.message()}"
        }
    }
}
