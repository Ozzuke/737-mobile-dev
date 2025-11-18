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
import kotlinx.coroutines.flow.collect
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
    val selectedPreset: String = "24h",
    val selectedPatientId: String? = null,
    val preferredUnit: String? = null,
    val isClinician: Boolean = false
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

    init {
        // Observe preferred unit setting continuously
        viewModelScope.launch {
            preferencesRepository.getPreferredUnit().collect { unit ->
                _homeState.value = _homeState.value.copy(preferredUnit = unit)
            }
        }
    }

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
     * Set the clinician mode, affecting UI and data fetching logic
     */
    fun setClinicianMode(isClinician: Boolean) {
        if (_homeState.value.isClinician != isClinician) {
            _homeState.value = _homeState.value.copy(isClinician = isClinician)
            if (!isClinician) {
                // If switching out of clinician mode, clear patient selection
                _homeState.value = _homeState.value.copy(selectedPatientId = null)
            }
        }
    }

    /**
     * Set the selected patient ID for clinicians
     */
    fun setSelectedPatientId(patientId: String?) {
        _homeState.value = _homeState.value.copy(selectedPatientId = patientId)
        if (patientId == null && _homeState.value.isClinician) {
            // If clinician mode and patient ID is cleared, reset dataset state
            clearAllDatasetState()
        } else {
            // Refetch data for the new patient selection
            fetchLatestData()
        }
    }

    /**
     * Fetch the active dataset (from preferences) or latest dataset if none is set
     */
    fun fetchLatestData(preset: String = _homeState.value.selectedPreset) {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true, error = null, selectedPreset = preset)

            val patientId = _homeState.value.selectedPatientId
            if (_homeState.value.isClinician && patientId == null) {
                // Don't attempt to fetch data if in clinician mode without a patient selected
                _homeState.value = _homeState.value.copy(isLoading = false)
                return@launch
            }

            // Check connectivity
            checkConnectivity()

            // Get active dataset ID from preferences
            val activeDatasetId = preferencesRepository.getActiveDatasetId().first()

            if (activeDatasetId != null) {
                // Use the active dataset
                fetchDatasetById(activeDatasetId, preset, patientId)
            } else {
                // No active dataset set, use the latest one and save it as active
                repository.getDatasets(patientId)
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
                        fetchDatasetData(latestDataset.datasetId, preset, patientId)

                        // Analyze will be triggered inside fetchDatasetData after data is confirmed
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
    private suspend fun fetchDatasetById(datasetId: String, preset: String, patientId: String?) {
        repository.getDatasets(patientId)
            .onSuccess { datasets ->
                if (datasets.isEmpty()) {
                    clearAllDatasetState()
                    return@onSuccess
                }

                val dataset = datasets.firstOrNull { it.datasetId == datasetId }
                if (dataset != null) {
                    _homeState.value = _homeState.value.copy(latestDataset = dataset)
                    fetchDatasetData(datasetId, preset, patientId)
                    // Analyze will be triggered inside fetchDatasetData after data is confirmed
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
    private suspend fun fetchDatasetData(datasetId: String, preset: String, patientId: String?) {
        repository.getDatasetData(datasetId, preset, patientId)
            .onSuccess { data ->
                val latestPoint = data.overlayDays.lastOrNull()?.points?.lastOrNull()
                val latestGlucose = latestPoint?.glucose

                // Derive rowCount and interval from overlay if original dataset summary lacks them
                val pointsTotal = data.overlayDays.sumOf { it.points.size }
                val derivedInterval = runCatching {
                    val firstDay = data.overlayDays.firstOrNull()
                    val pts = firstDay?.points
                    if (pts != null && pts.size >= 2) (pts[1].minute - pts[0].minute).coerceAtLeast(1) else data.resolutionMin
                }.getOrElse { data.resolutionMin }

                val currentSummary = _homeState.value.latestDataset
                val enrichedSummary = if (currentSummary != null) {
                    if (currentSummary.rowCount == 0 || currentSummary.samplingIntervalMin == 0) {
                        currentSummary.copy(
                            rowCount = if (currentSummary.rowCount == 0) pointsTotal else currentSummary.rowCount,
                            samplingIntervalMin = if (currentSummary.samplingIntervalMin == 0) derivedInterval else currentSummary.samplingIntervalMin
                        )
                    } else currentSummary
                } else null

                // Update state
                _homeState.value = _homeState.value.copy(
                    datasetData = data,
                    latestGlucose = latestGlucose,
                    latestDataset = enrichedSummary,
                    isLoading = false
                )
                // Trigger analysis only when we have points
                if (data.overlayDays.any { it.points.isNotEmpty() }) {
                    fetchAnalysis(datasetId, preset, patientId)
                }
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
    private suspend fun fetchAnalysis(datasetId: String, preset: String, patientId: String?) {
        repository.analyzeDataset(datasetId, preset, patientId = patientId)
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
     * Clear all dataset-related state
     */
    fun clearAllDatasetState() {
        _homeState.value = _homeState.value.copy(
            latestDataset = null,
            datasetData = null,
            analysis = null,
            latestGlucose = null,
            status = null,
            error = null,
            isLoading = false
        )
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
