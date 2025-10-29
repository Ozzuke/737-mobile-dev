package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.domain.model.AnalysisResult
import com.example.project.domain.model.DatasetSummary
import com.example.project.domain.repository.CgmApiRepository
import com.example.project.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing CGM API data and UI state
 * Handles loading states, error handling, and data fetching from the API
 */
class CgmApiViewModel(
    private val repository: CgmApiRepository
) : ViewModel() {

    // UI State for datasets list
    private val _datasetsState = MutableStateFlow<UiState<List<DatasetSummary>>>(UiState.Idle)
    val datasetsState: StateFlow<UiState<List<DatasetSummary>>> = _datasetsState.asStateFlow()

    // UI State for analysis results
    private val _analysisState = MutableStateFlow<UiState<AnalysisResult>>(UiState.Idle)
    val analysisState: StateFlow<UiState<AnalysisResult>> = _analysisState.asStateFlow()

    // Health check state
    private val _healthState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val healthState: StateFlow<UiState<Boolean>> = _healthState.asStateFlow()

    /**
     * Check API health
     */
    fun checkHealth() {
        viewModelScope.launch {
            _healthState.value = UiState.Loading
            repository.checkHealth()
                .onSuccess { isHealthy ->
                    _healthState.value = UiState.Success(isHealthy)
                }
                .onFailure { error ->
                    _healthState.value = UiState.Error(
                        message = error.message ?: "Health check failed",
                        exception = error
                    )
                }
        }
    }

    /**
     * Fetch all datasets from the API
     */
    fun fetchDatasets() {
        viewModelScope.launch {
            _datasetsState.value = UiState.Loading
            repository.getDatasets()
                .onSuccess { datasets ->
                    _datasetsState.value = if (datasets.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(datasets)
                    }
                }
                .onFailure { error ->
                    _datasetsState.value = UiState.Error(
                        message = getErrorMessage(error),
                        exception = error
                    )
                }
        }
    }

    /**
     * Analyze a specific dataset
     * @param datasetId The dataset to analyze
     * @param preset The time preset (24h, 7d, or 14d)
     * @param lang The language code (default: en)
     */
    fun analyzeDataset(datasetId: String, preset: String = "24h", lang: String = "en") {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            repository.analyzeDataset(datasetId, preset, lang)
                .onSuccess { analysis ->
                    _analysisState.value = UiState.Success(analysis)
                }
                .onFailure { error ->
                    _analysisState.value = UiState.Error(
                        message = getErrorMessage(error),
                        exception = error
                    )
                }
        }
    }

    /**
     * Clear analysis state (useful when navigating back)
     */
    fun clearAnalysis() {
        _analysisState.value = UiState.Idle
    }

    /**
     * Retry fetching datasets
     */
    fun retryFetchDatasets() {
        fetchDatasets()
    }

    /**
     * Delete a dataset
     */
    fun deleteDataset(datasetId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteDataset(datasetId)
                .onSuccess {
                    // Refresh the datasets list after deletion
                    fetchDatasets()
                    onSuccess()
                }
                .onFailure { error ->
                    _datasetsState.value = UiState.Error(
                        message = "Failed to delete dataset: ${getErrorMessage(error)}",
                        exception = error
                    )
                }
        }
    }

    /**
     * Get user-friendly error message from exception
     */
    private fun getErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("Unable to resolve host") == true ||
            error.message?.contains("Failed to connect") == true ||
            error is java.net.UnknownHostException -> {
                "No internet connection. Please check your network and try again."
            }
            error.message?.contains("timeout") == true -> {
                "Request timeout. Please try again."
            }
            error.message?.contains("Authentication failed") == true -> {
                "Authentication failed. Please check your API key."
            }
            else -> {
                error.message ?: "An unexpected error occurred. Please try again."
            }
        }
    }
}

