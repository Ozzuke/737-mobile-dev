package com.example.project.ui.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.local.PreferencesRepository
import com.example.project.domain.model.AnalysisResult
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.DatasetSummary
import com.example.project.domain.model.RatingCategory
import com.example.project.domain.repository.CgmApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * State for the home screen
 */
data class HomeScreenState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val latestDataset: DatasetSummary? = null,
    val latestGlucose: Double? = null,
    val timestamp: String? = null,
    val status: RatingCategory? = null,
    val datasetData: DatasetData? = null,
    val analysis: AnalysisResult? = null,
    val selectedPreset: String = "24h"
)

/**
 * ViewModel for the main home screen
 * Fetches active or latest dataset, analysis, and glucose data from API
 */
class MainViewModel(
    private val repository: CgmApiRepository,
    private val preferencesRepository: PreferencesRepository,
    private val context: Context
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeScreenState())
    val homeState: StateFlow<HomeScreenState> = _homeState.asStateFlow()

    // Don't fetch in init - let the UI trigger it with LaunchedEffect
    // This prevents crashes during ViewModel initialization

    /**
     * Check internet connectivity
     */
    private fun checkConnectivity() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            val isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            _homeState.value = _homeState.value.copy(isOnline = isOnline)
        } catch (e: Exception) {
            // If connectivity check fails, assume online and let network calls handle errors
            _homeState.value = _homeState.value.copy(isOnline = true)
        }
    }

    /**
     * Fetch the active dataset (from preferences) or latest dataset if none is set
     */
    fun fetchLatestData(preset: String = _homeState.value.selectedPreset) {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true, error = null, selectedPreset = preset)

            // Check connectivity
            checkConnectivity()

            // Get active dataset ID from preferences
            val activeDatasetId = preferencesRepository.getActiveDatasetId().first()

            if (activeDatasetId != null) {
                // Use the active dataset
                fetchDatasetById(activeDatasetId, preset)
            } else {
                // No active dataset set, use the latest one and save it as active
                repository.getDatasets()
                    .onSuccess { datasets ->
                        if (datasets.isEmpty()) {
                            _homeState.value = _homeState.value.copy(
                                isLoading = false,
                                error = "No datasets available. Please upload a CSV file."
                            )
                            return@onSuccess
                        }

                        // Get the most recent dataset (first in list, sorted by created_at)
                        val latestDataset = datasets.first()
                        _homeState.value = _homeState.value.copy(latestDataset = latestDataset)

                        // Save as active dataset for future opens
                        preferencesRepository.setActiveDatasetId(latestDataset.datasetId)

                        // Fetch dataset data (overlay)
                        fetchDatasetData(latestDataset.datasetId, preset)

                        // Fetch analysis
                        fetchAnalysis(latestDataset.datasetId, preset)
                    }
                    .onFailure { error ->
                        _homeState.value = _homeState.value.copy(
                            isLoading = false,
                            error = getErrorMessage(error)
                        )
                    }
            }
        }
    }

    /**
     * Fetch a specific dataset by ID
     */
    private suspend fun fetchDatasetById(datasetId: String, preset: String) {
        repository.getDatasets()
            .onSuccess { datasets ->
                val dataset = datasets.firstOrNull { it.datasetId == datasetId }
                if (dataset != null) {
                    _homeState.value = _homeState.value.copy(latestDataset = dataset)
                    fetchDatasetData(datasetId, preset)
                    fetchAnalysis(datasetId, preset)
                } else {
                    _homeState.value = _homeState.value.copy(
                        isLoading = false,
                        error = "Active dataset not found. It may have been deleted."
                    )
                }
            }
            .onFailure { error ->
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(error)
                )
            }
    }

    /**
     * Fetch dataset data (overlay) for the graph
     */
    private suspend fun fetchDatasetData(datasetId: String, preset: String) {
        repository.getDatasetData(datasetId, preset)
            .onSuccess { data ->
                // Extract latest glucose value
                val latestPoint = data.overlayDays.lastOrNull()?.points?.lastOrNull()
                val latestGlucose = latestPoint?.glucose

                _homeState.value = _homeState.value.copy(
                    datasetData = data,
                    latestGlucose = latestGlucose,
                    isLoading = false
                )
            }
            .onFailure { error ->
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    error = "Failed to fetch dataset data: ${getErrorMessage(error)}"
                )
            }
    }

    /**
     * Fetch analysis for the dataset
     */
    private suspend fun fetchAnalysis(datasetId: String, preset: String) {
        repository.analyzeDataset(datasetId, preset)
            .onSuccess { analysis ->
                _homeState.value = _homeState.value.copy(
                    analysis = analysis,
                    status = analysis.overallRating.category
                )
            }
            .onFailure { error ->
                _homeState.value = _homeState.value.copy(
                    error = "Failed to fetch analysis: ${getErrorMessage(error)}"
                )
            }
    }

    /**
     * Change the selected timeframe preset
     */
    fun changePreset(preset: String) {
        if (preset != _homeState.value.selectedPreset) {
            fetchLatestData(preset)
        }
    }

    /**
     * Retry fetching data
     */
    fun retry() {
        fetchLatestData()
    }

    /**
     * Convert exception to user-friendly message
     */
    private fun getErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("Unable to resolve host") == true ||
            error is java.net.UnknownHostException -> {
                "No internet connection. Please check your network."
            }
            error.message?.contains("timeout") == true -> {
                "Request timeout. Please try again."
            }
            else -> {
                error.message ?: "An unexpected error occurred."
            }
        }
    }
}
